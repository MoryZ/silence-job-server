package com.old.silence.job.server.job.task.support.executor.job;

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
import com.github.rholder.retry.RetryListener;
import com.old.silence.job.common.client.dto.ExecuteResult;
import com.old.silence.job.common.client.dto.request.DispatchJobRequest;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.JobTaskStatus;
import com.old.silence.job.common.model.ApiResult;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.cache.CacheRegisterTable;
import com.old.silence.job.server.common.dto.JobLogMetaDTO;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;
import com.old.silence.job.server.common.rpc.client.RequestBuilder;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.job.task.client.JobRpcClient;
import com.old.silence.job.server.job.task.dto.JobExecutorResultDTO;
import com.old.silence.job.server.job.task.dto.JobTaskFailAlarmEventDTO;
import com.old.silence.job.server.job.task.dto.RealJobExecutorDTO;
import com.old.silence.job.server.job.task.support.ClientCallbackHandler;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.job.task.support.alarm.event.JobTaskFailAlarmEvent;
import com.old.silence.job.server.job.task.support.callback.ClientCallbackContext;
import com.old.silence.job.server.job.task.support.callback.ClientCallbackFactory;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;


@Component(ActorGenerator.REAL_JOB_EXECUTOR_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)

public class RequestClientActor extends AbstractActor {


    private static final Logger log = LoggerFactory.getLogger(RequestClientActor.class);

    private static void taskExecuteFailure(RealJobExecutorDTO realJobExecutorDTO, String message) {
        ActorRef actorRef = ActorGenerator.jobTaskExecutorResultActor();
        JobExecutorResultDTO jobExecutorResultDTO = JobTaskConverter.INSTANCE.toJobExecutorResultDTO(realJobExecutorDTO);
        jobExecutorResultDTO.setTaskStatus(JobTaskStatus.FAIL);
        jobExecutorResultDTO.setMessage(message);
        actorRef.tell(jobExecutorResultDTO, actorRef);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RealJobExecutorDTO.class, realJobExecutorDTO -> {
            try {
                doExecute(realJobExecutorDTO);
            } catch (Exception e) {
                log.error("请求客户端发生异常", e);
            }
        }).build();
    }

    private void doExecute(RealJobExecutorDTO realJobExecutorDTO) {
        long nowMilli = DateUtils.toNowMilli();
        // 检查客户端是否存在
        RegisterNodeInfo registerNodeInfo = CacheRegisterTable.getServerNode(
                realJobExecutorDTO.getGroupName(),
                realJobExecutorDTO.getNamespaceId(),
                realJobExecutorDTO.getClientId());
        if (Objects.isNull(registerNodeInfo)) {
            taskExecuteFailure(realJobExecutorDTO, "客户端不存在");
            JobLogMetaDTO jobLogMetaDTO = JobTaskConverter.INSTANCE.toJobLogDTO(realJobExecutorDTO);
            jobLogMetaDTO.setTimestamp(nowMilli);
            if (realJobExecutorDTO.getRetryStatus()) {
                SilenceJobLog.REMOTE.error("taskId:[{}] 任务调度失败执行重试. 失败原因: 无可执行的客户端. 重试次数:[{}]. <|>{}<|>",
                        realJobExecutorDTO.getTaskId(), realJobExecutorDTO.getRetryCount(), jobLogMetaDTO);
            } else {
                SilenceJobLog.REMOTE.error("taskId:[{}] 任务调度失败. 失败原因: 无可执行的客户端 <|>{}<|>", realJobExecutorDTO.getTaskId(),
                        jobLogMetaDTO);
            }
            return;
        }

        DispatchJobRequest dispatchJobRequest = JobTaskConverter.INSTANCE.toDispatchJobRequest(realJobExecutorDTO);

        // 兼容历史客户端版本正式版本即可删除
        dispatchJobRequest.setRetry(realJobExecutorDTO.getRetryStatus());

        try {
            // 构建请求客户端对象
            JobRpcClient rpcClient = buildRpcClient(registerNodeInfo, realJobExecutorDTO);
            ApiResult<Boolean> dispatch = rpcClient.dispatch(dispatchJobRequest);
            if (Objects.equals(dispatch.getCode(), 200) && Objects.equals(dispatch.getData(), Boolean.TRUE)) {
                SilenceJobLog.LOCAL.info("taskId:[{}] 任务调度成功.", realJobExecutorDTO.getTaskId());
            } else {
                // 客户端返回失败，则认为任务执行失败
                ClientCallbackHandler clientCallback = ClientCallbackFactory.getClientCallback(realJobExecutorDTO.getTaskType());
                ClientCallbackContext context = JobTaskConverter.INSTANCE.toClientCallbackContext(realJobExecutorDTO);
                context.setTaskStatus(JobTaskStatus.FAIL);
                context.setExecuteResult(ExecuteResult.failure(null, dispatch.getMessage()));
                clientCallback.callback(context);
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

            JobLogMetaDTO jobLogMetaDTO = JobTaskConverter.INSTANCE.toJobLogDTO(realJobExecutorDTO);
            jobLogMetaDTO.setTimestamp(nowMilli);
            if (realJobExecutorDTO.getRetryStatus()) {
                SilenceJobLog.REMOTE.error("taskId:[{}] 任务调度失败执行重试 重试次数:[{}]. <|>{}<|>", jobLogMetaDTO.getTaskId(),
                        realJobExecutorDTO.getRetryCount(), jobLogMetaDTO, throwable);
            } else {
                SilenceJobLog.REMOTE.error("taskId:[{}] 任务调度失败. <|>{}<|>",
                        jobLogMetaDTO.getTaskId(),
                        jobLogMetaDTO, throwable);
            }

            taskExecuteFailure(realJobExecutorDTO, throwable.getMessage());
            var jobTaskFailAlarmEventDTO = new JobTaskFailAlarmEventDTO();
            jobTaskFailAlarmEventDTO.setJobTaskBatchId(dispatchJobRequest.getTaskBatchId());
            jobTaskFailAlarmEventDTO.setNotifyScene(JobNotifyScene.JOB_TASK_ERROR);
            jobTaskFailAlarmEventDTO.setReason(throwable.getMessage());
            SilenceSpringContext.getContext().publishEvent(
                    new JobTaskFailAlarmEvent(jobTaskFailAlarmEventDTO));
        }

    }

    private JobRpcClient buildRpcClient(RegisterNodeInfo registerNodeInfo, RealJobExecutorDTO realJobExecutorDTO) {

        int maxRetryTimes = realJobExecutorDTO.getMaxRetryTimes();
        boolean retry = realJobExecutorDTO.getRetryStatus();
        return RequestBuilder.<JobRpcClient, Result>newBuilder()
                .nodeInfo(registerNodeInfo)
                .failRetry(maxRetryTimes > 0 && !retry)
                .retryTimes(maxRetryTimes)
                .retryInterval(realJobExecutorDTO.getRetryInterval())
                .retryListener(new JobExecutorRetryListener(realJobExecutorDTO))
                .client(JobRpcClient.class)
                .build();
    }

    public static class JobExecutorRetryListener implements RetryListener {

        private final RealJobExecutorDTO realJobExecutorDTO;

        public JobExecutorRetryListener(final RealJobExecutorDTO realJobExecutorDTO) {
            this.realJobExecutorDTO = realJobExecutorDTO;
        }

        @Override
        public <V> void onRetry(final Attempt<V> attempt) {
            // 负载节点
            if (attempt.hasException()) {
                SilenceJobLog.LOCAL.error("任务调度失败. taskInstanceId:[{}] count:[{}]",
                        realJobExecutorDTO.getTaskBatchId(), attempt.getAttemptNumber(), attempt.getExceptionCause());
                ClientCallbackHandler clientCallback = ClientCallbackFactory.getClientCallback(realJobExecutorDTO.getTaskType());
                ClientCallbackContext context = JobTaskConverter.INSTANCE.toClientCallbackContext(realJobExecutorDTO);
                context.setTaskStatus(JobTaskStatus.FAIL);
                context.setExecuteResult(ExecuteResult.failure(null, "网络请求失败"));
                clientCallback.callback(context);
            }
        }
    }
}
