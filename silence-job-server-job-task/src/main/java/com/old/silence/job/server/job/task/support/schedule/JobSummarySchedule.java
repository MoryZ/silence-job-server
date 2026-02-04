package com.old.silence.job.server.job.task.support.schedule;

import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;
import com.old.silence.job.server.common.config.SystemProperties;
import com.old.silence.job.server.common.dto.JobTaskBatchReason;
import com.old.silence.job.server.common.schedule.AbstractSchedule;
import com.old.silence.job.server.common.triple.Pair;
import com.old.silence.job.server.domain.model.JobSummary;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobSummaryDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.vo.JobBatchSummaryResponseDO;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Job Dashboard
 */
@Component
public class JobSummarySchedule extends AbstractSchedule implements Lifecycle {
    private final JobTaskBatchDao jobTaskBatchDao;
    private final JobSummaryDao jobSummaryDao;
    private final JobDao jobDao;
    private final SystemProperties systemProperties;

    public JobSummarySchedule(JobTaskBatchDao jobTaskBatchDao, JobSummaryDao jobSummaryDao,
                              JobDao jobDao, SystemProperties systemProperties) {
        this.jobTaskBatchDao = jobTaskBatchDao;
        this.jobSummaryDao = jobSummaryDao;
        this.jobDao = jobDao;
        this.systemProperties = systemProperties;
    }

    @Override
    public String lockName() {
        return "jobSummaryDashboard";
    }

    @Override
    public String lockAtMost() {
        return "PT1M";
    }

    @Override
    public String lockAtLeast() {
        return "PT20S";
    }

    @Override
    protected void doExecute() {
        var zoneId = ZoneId.systemDefault();
        try {
            for (int i = 0; i < systemProperties.getSummaryDay(); i++) {

                // 定时按日实时查询统计数据（00:00:00 - 23:59:59）
                var beginTime = Instant.now().atZone(zoneId)
                        .withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant();
                var endTime = Instant.now().atZone(zoneId)
                        .withHour(23).withMinute(59).withSecond(59).withNano(999999999).toInstant();
                LambdaQueryWrapper<JobTaskBatch> wrapper = new LambdaQueryWrapper<JobTaskBatch>()
                        .eq(JobTaskBatch::getSystemTaskType, SystemTaskType.JOB)
                        .between(JobTaskBatch::getCreatedDate, beginTime, endTime)
                        .groupBy(JobTaskBatch::getNamespaceId, JobTaskBatch::getGroupName,
                                JobTaskBatch::getJobId, JobTaskBatch::getTaskBatchStatus, JobTaskBatch::getOperationReason);
                List<JobBatchSummaryResponseDO> summaryResponseDOList = jobTaskBatchDao.selectJobBatchSummaryList(wrapper);
                if (CollectionUtils.isEmpty(summaryResponseDOList)) {
                    continue;
                }

                // insertOrUpdate
                List<JobSummary> jobSummaryList = jobSummaryList(beginTime, summaryResponseDOList);

                List<JobSummary> jobSummaries = jobSummaryDao.selectList(new LambdaQueryWrapper<JobSummary>()
                        .eq(JobSummary::getTriggerAt, beginTime)
                        .eq(JobSummary::getSystemTaskType, SystemTaskType.JOB)
                        .in(JobSummary::getBusinessId, StreamUtils.toSet(jobSummaryList, JobSummary::getBusinessId)));

                Map<Pair<BigInteger, Instant>, JobSummary> summaryMap = StreamUtils.toIdentityMap(jobSummaries,
                        jobSummary -> Pair.of(jobSummary.getBusinessId(), jobSummary.getTriggerAt()));

                List<JobSummary> waitInserts = Lists.newArrayList();
                List<JobSummary> waitUpdates = Lists.newArrayList();
                for (final JobSummary jobSummary : jobSummaryList) {
                    if (Objects.isNull(
                            summaryMap.get(Pair.of(jobSummary.getBusinessId(), jobSummary.getTriggerAt())))) {
                        waitInserts.add(jobSummary);
                    } else {
                        waitUpdates.add(jobSummary);
                    }
                }

                int updateTotalJobSummary = 0;
                if (CollectionUtils.isNotEmpty(waitUpdates)) {
                    updateTotalJobSummary = jobSummaryDao.updateBatch(waitUpdates);
                }

                int insertTotalJobSummary = 0;
                if (CollectionUtils.isNotEmpty(waitInserts)) {
                    insertTotalJobSummary = jobSummaryDao.insertBatch(waitInserts);
                }

                SilenceJobLog.LOCAL.debug(
                        "job summary dashboard success todayFrom:[{}] todayTo:[{}] updateTotalJobSummary:[{}] insertTotalJobSummary:[{}]",
                        beginTime, endTime, updateTotalJobSummary, insertTotalJobSummary);
            }
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("job summary dashboard log error", e);
        }
    }

    private List<JobSummary> jobSummaryList(Instant triggerAt,
                                            List<JobBatchSummaryResponseDO> summaryResponseDOList) {
        List<JobSummary> jobSummaryList = new ArrayList<>();
        Map<BigInteger, List<JobBatchSummaryResponseDO>> jobIdListMap = summaryResponseDOList.parallelStream()
                .collect(Collectors.groupingBy(JobBatchSummaryResponseDO::getJobId));
        for (Map.Entry<BigInteger, List<JobBatchSummaryResponseDO>> job : jobIdListMap.entrySet()) {
            var value = job.getValue();
            JobSummary jobSummary = new JobSummary();
            jobSummary.setBusinessId(job.getKey());
            jobSummary.setTriggerAt(triggerAt);
            jobSummary.setNamespaceId(value.get(0).getNamespaceId());
            jobSummary.setGroupName(value.get(0).getGroupName());
            jobSummary.setSystemTaskType(SystemTaskType.JOB);
            jobSummary.setSuccessNum(value.stream().mapToInt(JobBatchSummaryResponseDO::getSuccessNum).sum());
            jobSummary.setFailNum(value.stream().mapToInt(JobBatchSummaryResponseDO::getFailNum).sum());
            jobSummary.setStopNum(value.stream().mapToInt(JobBatchSummaryResponseDO::getStopNum).sum());
            jobSummary.setCancelNum(value.stream().mapToInt(JobBatchSummaryResponseDO::getCancelNum).sum());

            jobSummary.setFailReason(
                    JSON.toJSONString(jobTaskBatchReasonList(JobTaskBatchStatus.FAIL, value)));
            jobSummary.setStopReason(
                    JSON.toJSONString(jobTaskBatchReasonList(JobTaskBatchStatus.STOP, value)));
            jobSummary.setCancelReason(JSON.toJSONString(
                    jobTaskBatchReasonList(JobTaskBatchStatus.CANCEL, value)));
            jobSummaryList.add(jobSummary);
        }
        return jobSummaryList;
    }

    /**
     * 批次状态查询 (操作原因 && 总数)
     */
    private List<JobTaskBatchReason> jobTaskBatchReasonList(JobTaskBatchStatus jobTaskBatchStatus,
                                                            List<JobBatchSummaryResponseDO> jobBatchSummaryResponseDOList) {
        List<JobTaskBatchReason> jobTaskBatchReasonArrayList = new ArrayList<>();
        List<JobBatchSummaryResponseDO> summaryResponseDOList = jobBatchSummaryResponseDOList.stream()
                .filter(i -> jobTaskBatchStatus == i.getTaskBatchStatus()).collect(Collectors.toList());
        for (JobBatchSummaryResponseDO jobBatchSummaryResponseDO : summaryResponseDOList) {
            JobTaskBatchReason jobTaskBatchReason = new JobTaskBatchReason();
            jobTaskBatchReason.setReason(jobBatchSummaryResponseDO.getOperationReason());
            jobTaskBatchReason.setTotal(jobBatchSummaryResponseDO.getOperationReasonTotal());
            jobTaskBatchReasonArrayList.add(jobTaskBatchReason);
        }
        return jobTaskBatchReasonArrayList;
    }

    @Override
    public void start() {
        taskScheduler.scheduleAtFixedRate(this::execute, Duration.parse("PT1M"));
    }

    @Override
    public void close() {

    }
}
