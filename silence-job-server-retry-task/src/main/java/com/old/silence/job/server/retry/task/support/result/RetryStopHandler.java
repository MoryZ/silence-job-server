package com.old.silence.job.server.retry.task.support.result;

import cn.hutool.core.lang.Assert;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import com.old.silence.job.common.enums.RetryOperationReason;
import com.old.silence.job.common.enums.RetryStatus;
import com.old.silence.job.server.domain.model.Retry;
import com.old.silence.job.server.domain.model.RetryTask;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryTaskDao;

import java.time.Instant;

import static com.old.silence.job.common.enums.RetryTaskStatus.STOP;

/**
 * <p>
 * 客户端触发停止重试指令, 重试挂起
 * </p>
 *
 */
@Component

public class RetryStopHandler extends AbstractRetryResultHandler {
    private final TransactionTemplate transactionTemplate;
    private final RetryTaskDao retryTaskDao;
    private final RetryDao retryDao;

    public RetryStopHandler(TransactionTemplate transactionTemplate, RetryTaskDao retryTaskDao, RetryDao retryDao) {
        this.transactionTemplate = transactionTemplate;
        this.retryTaskDao = retryTaskDao;
        this.retryDao = retryDao;
    }

    @Override
    public boolean supports(RetryResultContext context) {
        RetryOperationReason reasonEnum = context.getOperationReason();
        return STOP.equals(context.getTaskStatus())
                && RetryOperationReason.CLIENT_TRIGGER_RETRY_STOP.equals(reasonEnum);
    }

    @Override
    public void doHandler(RetryResultContext context) {
        transactionTemplate.execute((status) -> {

            Retry retry = new Retry();
            retry.setId(context.getRetryId());
            retry.setRetryStatus(RetryStatus.SUSPEND);
            retry.setUpdatedDate(Instant.now());
            retry.setRetryCount(retry.getRetryCount() + 1);
            Assert.isTrue(1 == retryDao.updateById(retry),
                    () -> new SilenceJobServerException("更新重试任务失败. groupName:[{}]",
                            retry.getGroupName()));

            RetryTask retryTask = new RetryTask();
            retryTask.setId(context.getRetryTaskId());
            retryTask.setOperationReason(context.getOperationReason());
            retryTask.setTaskStatus(STOP);
            Assert.isTrue(1 == retryTaskDao.updateById(retryTask),
                    () -> new SilenceJobServerException("更新重试任务失败. groupName:[{}]", retry.getGroupName()));

            return null;
        });
    }
}
