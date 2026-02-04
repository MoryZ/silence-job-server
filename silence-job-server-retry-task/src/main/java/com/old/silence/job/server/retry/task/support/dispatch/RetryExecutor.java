package com.old.silence.job.server.retry.task.support.dispatch;

import cn.hutool.core.lang.Assert;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.RetryOperationReason;
import com.old.silence.job.common.enums.RetryStatus;
import com.old.silence.job.common.enums.RetryTaskExecutorSceneEnum;
import com.old.silence.job.common.enums.RetryTaskStatus;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.cache.CacheRegisterTable;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;
import com.old.silence.job.server.common.handler.ClientNodeAllocateHandler;
import com.old.silence.job.server.common.util.ClientInfoUtils;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.domain.model.Retry;
import com.old.silence.job.server.domain.model.RetrySceneConfig;
import com.old.silence.job.server.domain.model.RetryTask;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryTaskDao;
import com.old.silence.job.server.infrastructure.persistence.dao.SceneConfigDao;
import com.old.silence.job.server.common.pekko.ActorGenerator;
import com.old.silence.job.server.retry.task.dto.RequestCallbackExecutorDTO;
import com.old.silence.job.server.retry.task.dto.RequestRetryExecutorDTO;
import com.old.silence.job.server.retry.task.dto.RetryTaskExecuteDTO;
import com.old.silence.job.server.retry.task.dto.RetryTaskFailAlarmEventDTO;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;
import com.old.silence.job.server.retry.task.support.event.RetryTaskFailAlarmEvent;
import com.old.silence.job.server.retry.task.support.handler.RetryTaskStopHandler;
import com.old.silence.job.server.retry.task.support.timer.RetryTimeoutCheckTask;
import com.old.silence.job.server.retry.task.support.timer.RetryTimerWheel;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Objects;


@Component(ActorGenerator.RETRY_EXECUTOR_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)

public class RetryExecutor extends AbstractActor {
    private final RetryDao retryDao;
    private final RetryTaskDao retryTaskDao;
    private final SceneConfigDao sceneConfigDao;
    private final ClientNodeAllocateHandler clientNodeAllocateHandler;
    private final RetryTaskStopHandler retryTaskStopHandler;

    public RetryExecutor(RetryDao retryDao, RetryTaskDao retryTaskDao,
                         SceneConfigDao sceneConfigDao, ClientNodeAllocateHandler clientNodeAllocateHandler,
                         RetryTaskStopHandler retryTaskStopHandler) {
        this.retryDao = retryDao;
        this.retryTaskDao = retryTaskDao;
        this.sceneConfigDao = sceneConfigDao;
        this.clientNodeAllocateHandler = clientNodeAllocateHandler;
        this.retryTaskStopHandler = retryTaskStopHandler;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryTaskExecuteDTO.class, execute -> {

            try {
                Assert.notNull(execute.getRetryId(), () -> new SilenceJobServerException("retryId can not be null"));
                Assert.notNull(execute.getRetryTaskId(), () -> new SilenceJobServerException("retryTaskId can not be null"));
                doExecute(execute);
            } catch (Exception e) {
                SilenceJobLog.LOCAL.error("Data scanner processing exception. [{}]", execute, e);
                updateRetryTaskStatus(execute.getRetryTaskId(), RetryTaskStatus.FAIL, RetryOperationReason.TASK_EXECUTION_ERROR);
            }

        }).build();
    }

    private void doExecute(RetryTaskExecuteDTO execute) {
        LambdaQueryWrapper<Retry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Retry::getId, execute.getRetryId());
        if (RetryTaskExecutorSceneEnum.MANUAL_RETRY.getScene() != execute.getRetryTaskExecutorScene()) {
            wrapper.eq(Retry::getRetryStatus, RetryStatus.RUNNING.getValue());
        }

        Retry retry = retryDao.selectOne(wrapper);
        if (Objects.isNull(retry)) {
            // 没有执行中的任务不执行调度
            updateRetryTaskStatus(execute.getRetryTaskId(), RetryTaskStatus.CANCEL,  RetryOperationReason.NOT_RUNNING_RETRY);
            return;
        }

        execute.setNamespaceId(retry.getNamespaceId());
        execute.setGroupName(retry.getGroupName());
        execute.setTaskType(retry.getTaskType());

        if (CollectionUtils.isEmpty(CacheRegisterTable.getServerNodeSet(retry.getGroupName(), retry.getNamespaceId()))) {
            // 无客户端不执行调度
            updateRetryTaskStatus(execute.getRetryTaskId(), RetryTaskStatus.CANCEL, RetryOperationReason.NOT_CLIENT);
            RetryTaskFailAlarmEventDTO toRetryTaskFailAlarmEventDTO =
                    RetryTaskConverter.INSTANCE.toRetryTaskFailAlarmEventDTO(retry, "无客户端节点",
                            JobNotifyScene.RETRY_NO_CLIENT_NODES_ERROR);
            SilenceSpringContext.getContext().publishEvent(new RetryTaskFailAlarmEvent(toRetryTaskFailAlarmEventDTO));
            return;
        }

        RetrySceneConfig retrySceneConfig = sceneConfigDao.selectOne(new LambdaQueryWrapper<RetrySceneConfig>()
                .eq(RetrySceneConfig::getSceneName, retry.getSceneName())
                .eq(RetrySceneConfig::getGroupName, retry.getGroupName())
                .eq(RetrySceneConfig::getNamespaceId, retry.getNamespaceId())
        );
        if (retrySceneConfig.getSceneStatus()) {
            // 场景已经关闭不执行调度
            updateRetryTaskStatus(execute.getRetryTaskId(), RetryTaskStatus.CANCEL, RetryOperationReason.SCENE_CLOSED);
            return;
        }

        // 获取执行的客户端
        RegisterNodeInfo serverNode = clientNodeAllocateHandler.getServerNode(retry.getId().toString(),
                retry.getGroupName(), retry.getNamespaceId(), retrySceneConfig.getRouteKey());
        updateRetryTaskStatus(execute.getRetryTaskId(), RetryTaskStatus.RUNNING,
                ClientInfoUtils.generate(serverNode));

        if (SystemTaskType.CALLBACK.equals(retry.getTaskType())) {
            // 请求客户端
            RequestCallbackExecutorDTO callbackExecutorDTO = RetryTaskConverter.INSTANCE.toRequestCallbackExecutorDTO(retrySceneConfig, retry);
            callbackExecutorDTO.setClientId(serverNode.getHostId());
            callbackExecutorDTO.setRetryTaskId(execute.getRetryTaskId());

            ActorRef actorRef = ActorGenerator.callbackRealTaskExecutorActor();
            actorRef.tell(callbackExecutorDTO, actorRef);
        } else {

            // 请求客户端
            RequestRetryExecutorDTO retryExecutorDTO = RetryTaskConverter.INSTANCE.toRealRetryExecutorDTO(retrySceneConfig, retry);
            retryExecutorDTO.setClientId(serverNode.getHostId());
            retryExecutorDTO.setRetryTaskId(execute.getRetryTaskId());

            ActorRef actorRef = ActorGenerator.retryRealTaskExecutorActor();
            actorRef.tell(retryExecutorDTO, actorRef);
        }

        // 运行中的任务，需要进行超时检查
        RetryTimerWheel.registerWithRetry(() -> new RetryTimeoutCheckTask(
                execute.getRetryTaskId(), execute.getRetryId(), retryTaskStopHandler, retryDao, retryTaskDao),
                // 加500ms是为了让尽量保证客户端自己先超时中断，防止客户端上报成功但是服务端已触发超时中断
                Duration.ofMillis(DateUtils.toEpochMilli(retrySceneConfig.getExecutorTimeout()) + 500));
    }

    private void updateRetryTaskStatus(BigInteger retryTaskId, RetryTaskStatus taskStatus, String clientInfo) {
        updateRetryTaskStatus(retryTaskId, taskStatus, RetryOperationReason.NONE, clientInfo);
    }

    private void updateRetryTaskStatus(BigInteger retryTaskId, RetryTaskStatus taskStatus, RetryOperationReason reasonEnum) {
        updateRetryTaskStatus(retryTaskId, taskStatus, reasonEnum, null);
    }

    private void updateRetryTaskStatus(BigInteger retryTaskId,
                                       RetryTaskStatus taskStatus,
                                       RetryOperationReason reasonEnum,
                                       String clientInfo) {
        RetryTask retryTask = new RetryTask();
        retryTask.setId(retryTaskId);
        retryTask.setTaskStatus(taskStatus);
        retryTask.setOperationReason(reasonEnum);
        retryTask.setClientInfo(clientInfo);
        retryTaskDao.updateById(retryTask);
    }
}
