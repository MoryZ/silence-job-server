package com.old.silence.job.server.retry.task.support.dispatch;

import kotlin.Result;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryException;
import com.google.common.collect.Maps;
import com.old.silence.job.common.client.dto.request.DispatchRetryRequest;
import com.old.silence.job.common.enums.RetryTaskStatus;
import com.old.silence.job.common.model.ApiResult;
import com.old.silence.job.common.model.SilenceJobHeaders;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.cache.CacheRegisterTable;
import com.old.silence.job.server.common.dto.JobLogMetaDTO;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;
import com.old.silence.job.server.common.dto.RetryLogMetaDTO;
import com.old.silence.job.server.common.rpc.client.RequestBuilder;
import com.old.silence.job.server.common.rpc.client.SilenceJobRetryListener;
import com.old.silence.job.server.common.util.ClientInfoUtils;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.domain.model.RetryTask;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryTaskDao;
import com.old.silence.job.server.common.pekko.ActorGenerator;
import com.old.silence.job.server.retry.task.client.RetryRpcClient;
import com.old.silence.job.server.retry.task.dto.RequestRetryExecutorDTO;
import com.old.silence.job.server.retry.task.dto.RetryExecutorResultDTO;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;
import com.old.silence.job.server.retry.task.support.RetryTaskLogConverter;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import java.util.Objects;


@Component(ActorGenerator.REAL_RETRY_EXECUTOR_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)

public class RequestRetryClientActor extends AbstractActor {

    private static final Logger log = LoggerFactory.getLogger(RequestRetryClientActor.class);
    private final RetryTaskDao retryTaskDao;

    public RequestRetryClientActor(RetryTaskDao retryTaskDao) {
        this.retryTaskDao = retryTaskDao;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RequestRetryExecutorDTO.class, realRetryExecutorDTO -> {
            try {
                doExecute(realRetryExecutorDTO);
            } catch (Exception e) {
                log.error("请求客户端发生异常", e);
            }
        }).build();
    }

    private void doExecute(RequestRetryExecutorDTO executorDTO) {
        long nowMilli = DateUtils.toNowMilli();
        // 检查客户端是否存在
        RegisterNodeInfo registerNodeInfo = CacheRegisterTable.getServerNode(
                executorDTO.getGroupName(),
                executorDTO.getNamespaceId(),
                executorDTO.getClientId()
        );

        if (Objects.isNull(registerNodeInfo)) {
            taskExecuteFailure(executorDTO, "客户端不存在");
            JobLogMetaDTO jobLogMetaDTO = RetryTaskConverter.INSTANCE.toJobLogDTO(executorDTO);
            jobLogMetaDTO.setTimestamp(nowMilli);
            SilenceJobLog.REMOTE.error("retryTaskId:[{}] 任务调度失败. 失败原因: 无可执行的客户端 <|>{}<|>", executorDTO.getRetryTaskId(),
                    jobLogMetaDTO);
            return;
        }

        DispatchRetryRequest dispatchJobRequest = RetryTaskConverter.INSTANCE.toDispatchRetryRequest(executorDTO);

        try {

            // 设置header
            SilenceJobHeaders silenceJobHeaders = new SilenceJobHeaders();
            silenceJobHeaders.setRetry(Boolean.TRUE);
            silenceJobHeaders.setRetryId(String.valueOf(executorDTO.getRetryId()));
            silenceJobHeaders.setDdl(executorDTO.getExecutorTimeout());

            // 构建请求客户端对象
            RetryRpcClient rpcClient = buildRpcClient(registerNodeInfo, executorDTO);
            ApiResult<Boolean> dispatch = rpcClient.dispatch(dispatchJobRequest, silenceJobHeaders);
            Boolean data = dispatch.getData();
            if (dispatch.getCode() == 200 && Objects.nonNull(data) && data) {
                SilenceJobLog.LOCAL.info("retryTaskId:[{}] 任务调度成功.", executorDTO.getRetryTaskId());
            } else {
                SilenceJobLog.LOCAL.error("retryTaskId:[{}] 任务调度失败. msg:[{}]", executorDTO.getRetryTaskId(), dispatch.getMessage());
                // 客户端返回失败，则认为任务执行失败
                taskExecuteFailure(executorDTO, dispatch.getMessage());
            }

        } catch (Exception e) {
            Throwable throwable;
            if (e.getClass().isAssignableFrom(RetryException.class)) {
                RetryException re = (RetryException) e;
                throwable = re.getLastFailedAttempt().getExceptionCause();
            } else if (e.getClass().isAssignableFrom(UndeclaredThrowableException.class)) {
                UndeclaredThrowableException re = (UndeclaredThrowableException) e;
                throwable = re.getUndeclaredThrowable();
            } else {
                throwable = e;
            }

            RetryLogMetaDTO retryTaskLogDTO = RetryTaskLogConverter.INSTANCE.toRetryLogMetaDTO(executorDTO);
            retryTaskLogDTO.setTimestamp(nowMilli);
            SilenceJobLog.REMOTE.error("retryTaskId:[{}] 任务调度失败. <|>{}<|>", retryTaskLogDTO.getRetryTaskId(),
                    retryTaskLogDTO, throwable);

            taskExecuteFailure(executorDTO, throwable.getMessage());
        }

    }

    public class RetryExecutorRetryListener implements SilenceJobRetryListener {

        private final Map<String, Object> properties;
        private final RequestRetryExecutorDTO executorDTO;

        public RetryExecutorRetryListener(final RequestRetryExecutorDTO realJobExecutorDTO) {
            this.executorDTO = realJobExecutorDTO;
            this.properties = Maps.newHashMap();
        }

        @Override
        public <V> void onRetry(final Attempt<V> attempt) {
            if (attempt.getAttemptNumber() > 1) {
                // 更新最新负载节点
                String hostId = (String) properties.get("HOST_ID");
                String hostIp = (String) properties.get("HOST_IP");
                Integer hostPort = (Integer) properties.get("HOST_PORT");

                RetryTask retryTask = new RetryTask();
                retryTask.setId(executorDTO.getRetryTaskId());
                RegisterNodeInfo realNodeInfo = new RegisterNodeInfo();
                realNodeInfo.setHostIp(hostIp);
                realNodeInfo.setHostPort(Integer.valueOf(hostPort));
                realNodeInfo.setHostId(hostId);
                retryTask.setClientInfo(ClientInfoUtils.generate(realNodeInfo));
                retryTaskDao.updateById(retryTask);
            }

        }

        @Override
        public Map<String, Object> properties() {
            return properties;
        }
    }

    private RetryRpcClient buildRpcClient(RegisterNodeInfo registerNodeInfo, RequestRetryExecutorDTO executorDTO) {
        return RequestBuilder.<RetryRpcClient, Result>newBuilder()
                .nodeInfo(registerNodeInfo)
                .failRetry(true)
                .failover(true)
                .retryTimes(3)
                .retryInterval(1)
                .routeKey(executorDTO.getRouteKey())
                .allocKey(String.valueOf(executorDTO.getRetryTaskId()))
                .retryListener(new RetryExecutorRetryListener(executorDTO))
                .client(RetryRpcClient.class)
                .build();
    }

    /**
     * 更新是执行状态
     *
     * @param executorDTO RequestRetryExecutorDTO
     * @param message 失败原因
     */
    private static void taskExecuteFailure(RequestRetryExecutorDTO executorDTO, String message) {
        ActorRef actorRef = ActorGenerator.retryTaskExecutorResultActor();
        RetryExecutorResultDTO executorResultDTO = RetryTaskConverter.INSTANCE.toRetryExecutorResultDTO(executorDTO);
        executorResultDTO.setExceptionMsg(message);
        executorResultDTO.setTaskStatus(RetryTaskStatus.FAIL);
        actorRef.tell(executorResultDTO, actorRef);
    }
}
