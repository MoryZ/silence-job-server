package com.old.silence.job.server.job.task.support.schedule;

import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;
import com.old.silence.job.server.common.config.SystemProperties;
import com.old.silence.job.server.common.dto.PartitionTask;
import com.old.silence.job.server.common.schedule.AbstractSchedule;
import com.old.silence.job.server.common.util.PartitionTaskUtils;
import com.old.silence.job.server.domain.model.JobLogMessage;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.infrastructure.persistence.dao.JobLogMessageDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.job.task.dto.JobPartitionTaskDTO;
import com.old.silence.job.server.job.task.support.JobTaskConverter;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Job清理日志 一小时运行一次
 */
@Component
public class JobClearLogSchedule extends AbstractSchedule implements Lifecycle {

    private final SystemProperties systemProperties;
    private final JobTaskBatchDao jobTaskBatchDao;
    private final JobTaskDao jobTaskDao;
    private final JobLogMessageDao jobLogMessageDao;
    private final TransactionTemplate transactionTemplate;

    public JobClearLogSchedule(SystemProperties systemProperties, JobTaskBatchDao jobTaskBatchDao,
                               JobTaskDao jobTaskDao, JobLogMessageDao jobLogMessageDao,
                               TransactionTemplate transactionTemplate) {
        this.systemProperties = systemProperties;
        this.jobTaskBatchDao = jobTaskBatchDao;
        this.jobTaskDao = jobTaskDao;
        this.jobLogMessageDao = jobLogMessageDao;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public String lockName() {
        return "jobClearLog";
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
                SilenceJobLog.LOCAL.error("job clear log storage error", systemProperties.getLogStorage());
                return;
            }
            // clean job log
            long total;
            Instant endTime = Instant.now().minus(systemProperties.getLogStorage(), ChronoUnit.DAYS);
            total = PartitionTaskUtils.process(startId -> jobTaskBatchList(startId, endTime),
                    this::processJobLogPartitionTasks, 0);

            SilenceJobLog.LOCAL.debug("Job clear success total:[{}]", total);
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("job clear log error", e);
        }
    }

    /**
     * JobLog List
     *
     * @param startId start id
     * @param endTime end time
     * @return List<JobPartitionTaskDTO>
     */
    private List<JobPartitionTaskDTO> jobTaskBatchList(Long startId, Instant endTime) {

        List<JobTaskBatch> jobTaskBatchList = jobTaskBatchDao.selectPage(
                        new Page<>(0, 1000),
                        new LambdaUpdateWrapper<JobTaskBatch>()
                                .ge(JobTaskBatch::getId, startId)
                                .le(JobTaskBatch::getCreatedDate, endTime)
                                .orderByAsc(JobTaskBatch::getId))
                .getRecords();
        return JobTaskConverter.INSTANCE.toJobTaskBatchPartitionTasks(jobTaskBatchList);
    }

    /**
     * clean table JobTaskBatch & JobTask & JobLogMessage
     *
     * @param partitionTasks partition tasks
     */
    public void processJobLogPartitionTasks(List<? extends PartitionTask> partitionTasks) {

        // Waiting for deletion JobTaskBatchList
        List<BigInteger> partitionTasksIds = StreamUtils.toList(partitionTasks, PartitionTask::getId);
        if (CollectionUtils.isEmpty(partitionTasksIds)) {
            return;
        }
        List<List<BigInteger>> idsPartition = Lists.partition(partitionTasksIds, 500);

        Set<BigInteger> jobTaskListIds = new HashSet<>();
        Set<BigInteger> jobLogMessageListIds = new HashSet<>();
        for (List<BigInteger> ids : idsPartition) {

            // Waiting for deletion JobTaskList
            List<JobTask> jobTaskList = jobTaskDao.selectList(new LambdaQueryWrapper<JobTask>()
                    .select(JobTask::getId)
                    .in(JobTask::getTaskBatchId, ids));
            if (!CollectionUtils.isEmpty(jobTaskList)) {
                Set<BigInteger> jobTask = jobTaskList.stream().map(JobTask::getId).collect(Collectors.toSet());
                jobTaskListIds.addAll(jobTask);
            }
            // Waiting for deletion JobLogMessageList
            List<JobLogMessage> jobLogMessageList = jobLogMessageDao.selectList(new LambdaQueryWrapper<JobLogMessage>()
                    .select(JobLogMessage::getId)
                    .in(JobLogMessage::getTaskBatchId, ids));
            if (!CollectionUtils.isEmpty(jobLogMessageList)) {
                Set<BigInteger> jobLogMessage = jobLogMessageList.stream().map(JobLogMessage::getId).collect(Collectors.toSet());
                jobLogMessageListIds.addAll(jobLogMessage);
            }
        }

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {

                idsPartition.forEach(jobTaskBatchDao::deleteBatchIds);
                if (!CollectionUtils.isEmpty(jobTaskListIds)) {
                    Lists.partition(jobTaskListIds.stream().collect(Collectors.toList()), 500).forEach(jobTaskDao::deleteBatchIds);
                }
                if (!CollectionUtils.isEmpty(jobLogMessageListIds)) {
                    Lists.partition(jobLogMessageListIds.stream().collect(Collectors.toList()), 500).forEach(jobLogMessageDao::deleteBatchIds);
                }
            }
        });
    }

    @Override
    public void start() {
        taskScheduler.scheduleAtFixedRate(this::execute, Duration.parse("PT4H"));
    }

    @Override
    public void close() {

    }
}
