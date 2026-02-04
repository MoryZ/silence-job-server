package com.old.silence.job.server.retry.task.support.result;

import cn.hutool.core.lang.Assert;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import com.old.silence.job.common.enums.RetryStatus;
import com.old.silence.job.common.enums.RetryTaskStatus;
import com.old.silence.job.server.domain.model.Retry;
import com.old.silence.job.server.domain.model.RetrySceneConfig;
import com.old.silence.job.server.domain.model.RetryTask;
import com.old.silence.job.server.domain.service.AccessTemplate;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryTaskDao;
import com.old.silence.job.server.retry.task.support.handler.CallbackRetryTaskHandler;

import java.time.Instant;
import java.util.Objects;

/**
 * <p>
 * 任务执行成功
 * </p>
 *
 */
@Component
public class RetrySuccessHandler extends AbstractRetryResultHandler {
    private final TransactionTemplate transactionTemplate;
    private final AccessTemplate accessTemplate;
    private final CallbackRetryTaskHandler callbackRetryTaskHandler;
    private final RetryTaskDao retryTaskDao;
    private final RetryDao retryDao;

    public RetrySuccessHandler(TransactionTemplate transactionTemplate, AccessTemplate accessTemplate,
                               CallbackRetryTaskHandler callbackRetryTaskHandler, RetryTaskDao retryTaskDao,
                               RetryDao retryDao) {
        this.transactionTemplate = transactionTemplate;
        this.accessTemplate = accessTemplate;
        this.callbackRetryTaskHandler = callbackRetryTaskHandler;
        this.retryTaskDao = retryTaskDao;
        this.retryDao = retryDao;
    }

    @Override
    public boolean supports(RetryResultContext context) {
        return Objects.equals(RetryTaskStatus.SUCCESS, context.getTaskStatus());
    }

    @Override
    public void doHandler(RetryResultContext context) {
        // 超过最大等级
        RetrySceneConfig retrySceneConfig =
                accessTemplate.getSceneConfigAccess().getSceneConfigByGroupNameAndSceneName(
                        context.getGroupName(), context.getSceneName(), context.getNamespaceId());
        Retry retry = retryDao.selectById(context.getRetryId());

        transactionTemplate.execute((status -> {

            retry.setRetryStatus(RetryStatus.FINISH);
            retry.setUpdatedDate(Instant.now());
            retry.setRetryCount(retry.getRetryCount() + 1);
            retry.setDeleted(false);
            Assert.isTrue(1 == retryDao.updateById(retry),
                    () -> new SilenceJobServerException("更新重试任务失败. groupName:[{}]",
                            retry.getGroupName()));

            RetryTask retryTask = new RetryTask();
            retryTask.setId(context.getRetryTaskId());
            retryTask.setTaskStatus(RetryTaskStatus.SUCCESS);
            Assert.isTrue(1 == retryTaskDao.updateById(retryTask),
                    () -> new SilenceJobServerException("更新重试任务失败. groupName:[{}]", retry.getGroupName()));

            // 创建一个回调任务
            callbackRetryTaskHandler.create(retry, retrySceneConfig);

            return null;
        }));
    }
}
