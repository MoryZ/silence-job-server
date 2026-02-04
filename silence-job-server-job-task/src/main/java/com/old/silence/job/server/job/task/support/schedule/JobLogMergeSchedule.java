package com.old.silence.job.server.job.task.support.schedule;

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
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;
import com.old.silence.job.server.common.config.SystemProperties;
import com.old.silence.job.server.common.dto.PartitionTask;
import com.old.silence.job.server.common.schedule.AbstractSchedule;
import com.old.silence.job.server.common.util.PartitionTaskUtils;
import com.old.silence.job.server.domain.model.JobLogMessage;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.infrastructure.persistence.dao.JobLogMessageDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.job.task.dto.JobPartitionTaskDTO;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.task.common.schedule.LogMergeUtils;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * jogLogMessage 日志合并归档
 */

@Component
public class JobLogMergeSchedule extends AbstractSchedule implements Lifecycle {

    private final SystemProperties systemProperties;
    private final JobTaskBatchDao jobTaskBatchDao;
    private final JobLogMessageDao jobLogMessageDao;
    private final TransactionTemplate transactionTemplate;

    public JobLogMergeSchedule(SystemProperties systemProperties, JobTaskBatchDao jobTaskBatchDao,
                               JobLogMessageDao jobLogMessageDao, TransactionTemplate transactionTemplate) {
        this.systemProperties = systemProperties;
        this.jobTaskBatchDao = jobTaskBatchDao;
        this.jobLogMessageDao = jobLogMessageDao;
        this.transactionTemplate = transactionTemplate;
    }


    @Override
    public String lockName() {
        return "jobLogMerge";
    }

    @Override
    public String lockAtMost() {
        return "PT1H";
    }

    @Override
    public String lockAtLeast() {
        return "PT1M";
    }

    @Override
    protected void doExecute() {
        try {
            // merge job log
            long total;
            Instant endTime = Instant.now().minus(systemProperties.getMergeLogDays(), ChronoUnit.DAYS);
            total = PartitionTaskUtils.process(startId -> jobTaskBatchList(startId, endTime),
                    this::processJobLogPartitionTasks, 0);

            SilenceJobLog.LOCAL.debug("job merge success total:[{}]", total);
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("job merge log error", e);
        }
    }

    /**
     * JobLog List
     *
     * @param startId start id
     * @param endTime endTime
     * @return List<JobPartitionTaskDTO>
     */
    private List<JobPartitionTaskDTO> jobTaskBatchList(Long startId, Instant endTime) {

        List<JobTaskBatch> jobTaskBatchList = jobTaskBatchDao.selectPage(
                new Page<>(0, 1000),
                new LambdaUpdateWrapper<JobTaskBatch>()
                        .ge(JobTaskBatch::getId, startId)
                        .in(JobTaskBatch::getTaskBatchStatus, JobTaskBatchStatus.COMPLETED)
                        .le(JobTaskBatch::getCreatedDate, endTime)
                        .orderByAsc(JobTaskBatch::getId)
        ).getRecords();
        return JobTaskConverter.INSTANCE.toJobTaskBatchPartitionTasks(jobTaskBatchList);
    }

    /**
     * merge job_log_message
     *
     * @param partitionTasks
     */
    public void processJobLogPartitionTasks(List<? extends PartitionTask> partitionTasks) {

        // Waiting for merge JobTaskBatchList
        List<BigInteger> ids = StreamUtils.toList(partitionTasks, PartitionTask::getId);
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        // Waiting for deletion JobLogMessageList
        List<JobLogMessage> jobLogMessageList = jobLogMessageDao.selectList(
                new LambdaQueryWrapper<JobLogMessage>().in(JobLogMessage::getTaskBatchId, ids));
        if (CollectionUtils.isEmpty(jobLogMessageList)) {
            return;
        }

        List<Map.Entry<BigInteger, List<JobLogMessage>>> jobLogMessageGroupList = jobLogMessageList.stream().collect(
                        groupingBy(JobLogMessage::getTaskId)).entrySet().stream()
                .filter(entry -> entry.getValue().size() >= 2).collect(toList());

        for (Map.Entry<BigInteger/*taskId*/, List<JobLogMessage>> jobLogMessageMap : jobLogMessageGroupList) {
            
            // 使用工具类合并日志
            LogMergeUtils.MergeResult mergeResult = LogMergeUtils.mergeLogs(
                    jobLogMessageMap.getValue(),
                    JobLogMessage::getMessage,
                    JobLogMessage::getId
            );
            
            List<BigInteger> jobLogMessageDeleteBatchIds = mergeResult.getDeleteIds();
            List<JobLogMessage> jobLogMessageInsertBatchIds = new ArrayList<>();

            // 分区并创建新日志记录
            List<List<String>> partitionMessages = LogMergeUtils.partitionMessages(
                    mergeResult.getMergedMessages(),
                    systemProperties.getMergeLogNum()
            );

            for (List<String> partitionMessage : partitionMessages) {
                // 深拷贝
                JobLogMessage jobLogMessage = JobTaskConverter.INSTANCE.toJobLogMessage(
                        jobLogMessageMap.getValue().getFirst());

                jobLogMessage.setLogNum(partitionMessage.size());
                jobLogMessage.setMessage(JSON.toJSONString(partitionMessage));
                jobLogMessageInsertBatchIds.add(jobLogMessage);
            }

            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(final TransactionStatus status) {

                    // 批量删除、更新日志
                    if (CollectionUtils.isNotEmpty(jobLogMessageDeleteBatchIds)) {
                        List<List<BigInteger>> partition = Lists.partition(jobLogMessageDeleteBatchIds, 500);
                        for (List<BigInteger> mid : partition) {
                            jobLogMessageDao.deleteBatchIds(mid);
                        }
                    }

                    if (CollectionUtils.isNotEmpty(jobLogMessageInsertBatchIds)) {
                        List<List<JobLogMessage>> partition = Lists.partition(jobLogMessageInsertBatchIds, 500);
                        for (List<JobLogMessage> jobLogMessages : partition) {
                            jobLogMessageDao.insertBatch(jobLogMessages);
                        }
                    }
                }
            });
        }

    }

    @Override
    public void start() {
        taskScheduler.scheduleAtFixedRate(this::execute, Duration.parse("PT1M"));
    }

    @Override
    public void close() {

    }
}
