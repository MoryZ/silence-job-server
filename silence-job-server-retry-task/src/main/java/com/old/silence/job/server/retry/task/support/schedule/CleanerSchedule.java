package com.old.silence.job.server.retry.task.support.schedule;

import cn.hutool.core.lang.Assert;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.RetryStatus;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;
import com.old.silence.job.server.common.config.SystemProperties;
import com.old.silence.job.server.common.dto.PartitionTask;
import com.old.silence.job.server.common.schedule.AbstractSchedule;
import com.old.silence.job.server.common.util.PartitionTaskUtils;
import com.old.silence.job.server.domain.model.Retry;
import com.old.silence.job.server.domain.model.RetryDeadLetter;
import com.old.silence.job.server.domain.model.RetryTask;
import com.old.silence.job.server.domain.model.RetryTaskLogMessage;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryDeadLetterDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryTaskDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryTaskLogMessageDao;
import com.old.silence.job.server.retry.task.dto.RetryPartitionTask;
import com.old.silence.job.server.retry.task.service.RetryDeadLetterConverter;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;
import com.old.silence.job.server.retry.task.support.event.RetryTaskFailDeadLetterAlarmEvent;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Retry清理线程
 * 1. 删除日志信息
 * 2. 删除重试已完成的数据
 * 3. 删除回调任务数据
 * 4. 删除调度日志 sj_retry_task
 * 5. 迁移到达最大重试的数据
 *
 */
@Component

public class CleanerSchedule extends AbstractSchedule implements Lifecycle {
    private final RetryDao retryDao;
    private final RetryTaskDao retryTaskDao;
    private final RetryDeadLetterDao retryDeadLetterDao;
    private final SystemProperties systemProperties;
    private final RetryTaskLogMessageDao retryTaskLogMessageDao;
    private final TransactionTemplate transactionTemplate;

    public CleanerSchedule(RetryDao retryDao, RetryTaskDao retryTaskDao,
                           RetryDeadLetterDao retryDeadLetterDao, SystemProperties systemProperties, 
                           RetryTaskLogMessageDao retryTaskLogMessageDao,
                           TransactionTemplate transactionTemplate) {
        this.retryDao = retryDao;
        this.retryTaskDao = retryTaskDao;
        this.retryDeadLetterDao = retryDeadLetterDao;
        this.systemProperties = systemProperties;
        this.retryTaskLogMessageDao = retryTaskLogMessageDao;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public String lockName() {
        return "clearLog";
    }

    @Override
    public String lockAtMost() {
        return "PT4H";
    }

    @Override
    public String lockAtLeast() {
        return "PT1M";
    }

    @Override
    protected void doExecute() {
        try {
            // 清除日志默认保存天数大于零、最少保留最近一天的日志数据
            if (systemProperties.getLogStorage() <= 1) {
                SilenceJobLog.LOCAL.error("retry clear log storage error", systemProperties.getLogStorage());
                return;
            }

            // clean retry log
            Instant endTime = Instant.now().minus(systemProperties.getLogStorage(), ChronoUnit.DAYS);
            long total = PartitionTaskUtils.process(startId -> retryTaskBatchList(startId, endTime),
                    this::processRetryLogPartitionTasks, 0);

            SilenceJobLog.LOCAL.debug("Retry clear success total:[{}]", total);
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("clear log error", e);
        }
    }

    /**
     * RetryLog List
     *
     */
    private List<RetryPartitionTask> retryTaskBatchList(Long startId, Instant endTime) {

        List<Retry> retryTaskList = retryDao.selectPage(
                        new Page<>(0, 500),
                        new LambdaUpdateWrapper<Retry>()
                                .ge(Retry::getId, startId)
                                .le(Retry::getCreatedDate, endTime)
                                .eq(Retry::getTaskType, SystemTaskType.RETRY.getValue())
                                .orderByAsc(Retry::getId))
                .getRecords();
        return RetryTaskConverter.INSTANCE.toRetryTaskLogPartitionTasks(retryTaskList);
    }

    /**
     * clean table RetryTaskLog & RetryTaskLogMessage
     *
     */
    public void processRetryLogPartitionTasks(List<? extends PartitionTask> partitionTasks) {

        List<BigInteger> retryIds = StreamUtils.toList(partitionTasks, PartitionTask::getId);
        if (CollectionUtils.isEmpty(retryIds)) {
            return;
        }

        // 查询回调数据
        List<Retry> cbRetries = retryDao.selectList(new LambdaQueryWrapper<Retry>()
                .select(Retry::getId).in(Retry::getParentId, retryIds));

        List<BigInteger> totalWaitRetryIds = Lists.newArrayList(retryIds);
        List<BigInteger> cbRetryIds = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(cbRetries)) {
            cbRetryIds = StreamUtils.toList(cbRetries, Retry::getId);
            totalWaitRetryIds.addAll(cbRetryIds);
        }

        List<RetryPartitionTask> retryPartitionTasks = (List<RetryPartitionTask>) partitionTasks;

        List<BigInteger> finishRetryIds = retryPartitionTasks.stream().filter(retryPartitionTask ->
                        RetryStatus.FINISH.equals(retryPartitionTask.getRetryStatus()))
                .map(PartitionTask::getId).collect(Collectors.toList());

        // 删除重试任务
        List<RetryTask> retryTaskList = retryTaskDao.selectList(new LambdaQueryWrapper<RetryTask>()
                .in(RetryTask::getRetryId, totalWaitRetryIds));

        // 删除重试日志信息
        List<RetryTaskLogMessage> retryTaskLogMessageList = retryTaskLogMessageDao.selectList(
                new LambdaQueryWrapper<RetryTaskLogMessage>()
                        .in(RetryTaskLogMessage::getRetryId, totalWaitRetryIds));

        List<BigInteger> finalCbRetryIds = cbRetryIds;
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {

                if (CollectionUtils.isNotEmpty(finalCbRetryIds)) {
                    // 删除回调数据
                    retryDao.deleteBatchIds(finalCbRetryIds);
                }

                if (CollectionUtils.isNotEmpty(finishRetryIds)) {
                    // 删除重试完成的数据
                    retryDao.deleteBatchIds(finishRetryIds);

                }

                // 删除重试任务
                if (!CollectionUtils.isEmpty(retryTaskList)) {
                    List<BigInteger> retryTaskIds = StreamUtils.toList(retryTaskList, RetryTask::getId);
                    Lists.partition(retryTaskIds, 500).forEach(retryTaskDao::deleteBatchIds);
                }

                if (!CollectionUtils.isEmpty(retryTaskLogMessageList)) {
                    List<BigInteger> retryTaskLogMessageIds = StreamUtils.toList(retryTaskLogMessageList, RetryTaskLogMessage::getId);
                    Lists.partition(retryTaskLogMessageIds, 500).forEach(retryTaskLogMessageDao::deleteBatchIds);
                }

            }
        });

        // 重试最大次数迁移死信表
        List<RetryPartitionTask> maxCountRetries = retryPartitionTasks.stream()
                .filter(retryPartitionTask ->
                        RetryStatus.MAX_COUNT.equals(retryPartitionTask.getRetryStatus()))
                .collect(Collectors.toList());
        moveDeadLetters(maxCountRetries);
    }

    /**
     * 迁移死信队列数据
     *
     * @param retries 待迁移数据
     */
    private void moveDeadLetters(List<RetryPartitionTask> retries) {
        if (CollectionUtils.isEmpty(retries)) {
            return;
        }

        List<RetryDeadLetter> retryDeadLetters = retries.stream().map(RetryDeadLetterConverter.INSTANCE::toRetryDeadLetter).collect(Collectors.toList());
        Instant now = Instant.now();
        for (RetryDeadLetter retryDeadLetter : retryDeadLetters) {
            retryDeadLetter.setCreatedDate(now);
        }

        Assert.isTrue(retryDeadLetters.size() == retryDeadLetterDao.insertBatch(retryDeadLetters),
                () -> new SilenceJobServerException("插入死信队列失败 [{}]", JSON.toJSONString(retryDeadLetters)));

        Assert.isTrue(retries.size() == retryDao.delete(new LambdaQueryWrapper<Retry>()
                        .in(Retry::getId, StreamUtils.toList(retries, RetryPartitionTask::getId))),
                () -> new SilenceJobServerException("删除重试数据失败 [{}]", JSON.toJSONString(retries)));

        SilenceSpringContext.getContext().publishEvent(new RetryTaskFailDeadLetterAlarmEvent(
                RetryTaskConverter.INSTANCE.toRetryTaskFailDeadLetterAlarmEventDTO(retryDeadLetters)
        ));
    }


    @Override
    public void start() {
        taskScheduler.scheduleAtFixedRate(this::execute, Duration.parse("PT4H"));
    }

    @Override
    public void close() {

    }
}
