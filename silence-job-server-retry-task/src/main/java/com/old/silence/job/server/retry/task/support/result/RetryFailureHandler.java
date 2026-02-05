package com.old.silence.job.server.retry.task.support.result;

import cn.hutool.core.lang.Assert;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.RetryOperationReason;
import com.old.silence.job.common.enums.RetryStatus;
import com.old.silence.job.common.enums.RetryTaskStatus;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.server.domain.model.Retry;
import com.old.silence.job.server.domain.model.RetrySceneConfig;
import com.old.silence.job.server.domain.model.RetryTask;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetrySceneConfigDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryTaskDao;
import com.old.silence.job.server.retry.task.dto.RetryTaskFailAlarmEventDTO;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;
import com.old.silence.job.server.retry.task.support.event.RetryTaskFailAlarmEvent;
import com.old.silence.job.server.retry.task.support.handler.CallbackRetryTaskHandler;

import java.time.Instant;
import java.util.Optional;

import static com.old.silence.job.common.enums.RetryTaskStatus.NOT_SUCCESS;

/**
 * <p>
 * 客户端执行重试失败、服务端调度时失败等场景导致的任务执行失败
 * </p>
 *
 */
@Component

public class RetryFailureHandler extends AbstractRetryResultHandler {
    private final RetrySceneConfigDao retrySceneConfigDao;
    private final CallbackRetryTaskHandler callbackRetryTaskHandler;
    private final TransactionTemplate transactionTemplate;
    private final RetryTaskDao retryTaskDao;
    private final RetryDao retryDao;

    public RetryFailureHandler(RetrySceneConfigDao retrySceneConfigDao, CallbackRetryTaskHandler callbackRetryTaskHandler,
                               TransactionTemplate transactionTemplate, RetryTaskDao retryTaskDao, RetryDao retryDao) {
        this.retrySceneConfigDao = retrySceneConfigDao;
        this.callbackRetryTaskHandler = callbackRetryTaskHandler;
        this.transactionTemplate = transactionTemplate;
        this.retryTaskDao = retryTaskDao;
        this.retryDao = retryDao;
    }

    @Override
    public boolean supports(RetryResultContext context) {
        RetryOperationReason reasonEnum = context.getOperationReason();
        return NOT_SUCCESS.contains(context.getTaskStatus())
                && !RetryOperationReason.CLIENT_TRIGGER_RETRY_STOP.equals(reasonEnum);
    }

    @Override
    public void doHandler(RetryResultContext context) {
        RetrySceneConfig retrySceneConfig = retrySceneConfigDao.selectOne(
                new LambdaQueryWrapper<RetrySceneConfig>()
                        .eq(RetrySceneConfig::getGroupName, context.getGroupName())
                        .eq(RetrySceneConfig::getSceneName, context.getSceneName())
                        .eq(RetrySceneConfig::getNamespaceId, context.getNamespaceId())
        );

        Retry retry = retryDao.selectById(context.getRetryId());
        transactionTemplate.execute(status -> {

            Integer maxRetryCount;
            if (SystemTaskType.CALLBACK.equals(retry.getTaskType())) {
                maxRetryCount = retrySceneConfig.getCbMaxCount();
            } else {
                maxRetryCount = retrySceneConfig.getMaxRetryCount();
            }

            if (maxRetryCount <= retry.getRetryCount() + 1) {
                retry.setRetryStatus(RetryStatus.MAX_COUNT);
                retry.setRetryCount(retry.getRetryCount() + 1);
                retry.setUpdatedDate(Instant.now());
                retry.setDeleted(false);
                Assert.isTrue(1 == retryDao.updateById(retry),
                        () -> new SilenceJobServerException("更新重试任务失败. groupName:[{}]", retry.getGroupName()));
                // 创建一个回调任务
                callbackRetryTaskHandler.create(retry, retrySceneConfig);
            } else if (context.isIncrementRetryCount()) {
                retry.setRetryCount(retry.getRetryCount() + 1);
                retry.setUpdatedDate(Instant.now());
                retry.setDeleted(false);
                Assert.isTrue(1 == retryDao.updateById(retry),
                        () -> new SilenceJobServerException("更新重试任务失败. groupName:[{}]", retry.getGroupName()));

            }

            RetryTask retryTask = new RetryTask();
            retryTask.setId(context.getRetryTaskId());
            retryTask.setTaskStatus(Optional.ofNullable(context.getTaskStatus()).orElse(RetryTaskStatus.FAIL));
            retryTask.setOperationReason(Optional.ofNullable(context.getOperationReason()).orElse(RetryOperationReason.NONE));
            Assert.isTrue(1 == retryTaskDao.updateById(retryTask),
                    () -> new SilenceJobServerException("更新重试任务失败. groupName:[{}]", retry.getGroupName()));

            RetryTaskFailAlarmEventDTO retryTaskFailAlarmEventDTO =
                    RetryTaskConverter.INSTANCE.toRetryTaskFailAlarmEventDTO(
                            retry, context.getExceptionMsg(), JobNotifyScene.RETRY_TASK_FAIL_ERROR);
            SilenceSpringContext.getContext().publishEvent(new RetryTaskFailAlarmEvent(retryTaskFailAlarmEventDTO));

            return null;
        });
    }
}
