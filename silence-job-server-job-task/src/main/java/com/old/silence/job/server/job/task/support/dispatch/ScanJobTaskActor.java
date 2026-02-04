package com.old.silence.job.server.job.task.support.dispatch;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.enums.JobTaskExecutorScene;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.WaitStrategy;
import com.old.silence.job.server.common.cache.CacheConsumerGroup;
import com.old.silence.job.server.common.config.SystemProperties;
import com.old.silence.job.server.common.dto.PartitionTask;
import com.old.silence.job.server.common.dto.ScanTask;
import com.old.silence.job.server.common.strategy.WaitStrategies;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.common.util.PartitionTaskUtils;
import com.old.silence.job.server.domain.model.GroupConfig;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.infrastructure.persistence.dao.GroupConfigDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;
import com.old.silence.job.server.job.task.dto.JobPartitionTaskDTO;
import com.old.silence.job.server.job.task.dto.JobTaskPrepareDTO;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.job.task.support.cache.ResidentTaskCache;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * JOB任务扫描
 */
@Component(ActorGenerator.SCAN_JOB_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)

public class ScanJobTaskActor extends AbstractActor {


    private static final Logger log = LoggerFactory.getLogger(ScanJobTaskActor.class);
    private final JobDao jobDao;
    private final SystemProperties systemProperties;
    private final GroupConfigDao groupConfigDao;

    public ScanJobTaskActor(JobDao jobDao, SystemProperties systemProperties,
                            GroupConfigDao groupConfigDao) {
        this.jobDao = jobDao;
        this.systemProperties = systemProperties;
        this.groupConfigDao = groupConfigDao;
    }

    /**
     * 需要重新计算触发时间的条件 1、不是常驻任务 2、常驻任务缓存的触发任务为空 3、常驻任务中的触发时间不是最新的
     */
    private static boolean needCalculateNextTriggerTime(JobPartitionTaskDTO partitionTask) {
        return !partitionTask.getResident();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ScanTask.class, config -> {

            try {
                doScan(config);
            } catch (Exception e) {
                SilenceJobLog.LOCAL.error("Data scanner processing exception. [{}]", config, e);
            }

        }).build();

    }

    private void doScan(final ScanTask scanTask) {
        if (CollectionUtils.isEmpty(scanTask.getBuckets())) {
            return;
        }

        long total = PartitionTaskUtils.process(startId -> listAvailableJobs(startId, scanTask),
                this::processJobPartitionTasks, 0);

        log.debug("job scan end. total:[{}]", total);
    }

    private void processJobPartitionTasks(List<? extends PartitionTask> partitionTasks) {

        List<Job> waitUpdateJobs = new ArrayList<>();
        List<JobTaskPrepareDTO> waitExecJobs = new ArrayList<>();
        long now = DateUtils.toNowMilli();
        for (PartitionTask partitionTask : partitionTasks) {
            processJob((JobPartitionTaskDTO) partitionTask, waitUpdateJobs, waitExecJobs, now);
        }

        // 批量更新
        jobDao.updateBatchNextTriggerAtById(waitUpdateJobs);

        for (final JobTaskPrepareDTO waitExecJob : waitExecJobs) {
            // 执行预处理阶段
            ActorRef actorRef = ActorGenerator.jobTaskPrepareActor();
            waitExecJob.setTaskExecutorScene(JobTaskExecutorScene.AUTO_JOB);
            actorRef.tell(waitExecJob, actorRef);
        }
    }

    private void processJob(JobPartitionTaskDTO partitionTask, final List<Job> waitUpdateJobs,
                            final List<JobTaskPrepareDTO> waitExecJobs, long now) {
        CacheConsumerGroup.addOrUpdate(partitionTask.getGroupName(), partitionTask.getNamespaceId());

        Job job = new Job();
        job.setId(partitionTask.getId());

        boolean triggerTask = true;
        Long nextTriggerAt = ResidentTaskCache.get(partitionTask.getId());
        if (needCalculateNextTriggerTime(partitionTask)) {
            // 更新下次触发时间
            nextTriggerAt = calculateNextTriggerTime(partitionTask, now);
        } else {
            // 若常驻任务的缓存时间为空则触发一次任务调度，说明常驻任务长时间未更新或者是系统刚刚启动
            triggerTask = Objects.isNull(nextTriggerAt);
            // 若出现常驻任务时间为null或者常驻任务的内存时间长期未更新, 刷新为now
            if (Objects.isNull(nextTriggerAt)
                    || (nextTriggerAt + DateUtils.toEpochMilli(SystemConstants.SCHEDULE_PERIOD)) < now) {
                nextTriggerAt = now;
            }
        }

        job.setNextTriggerAt(nextTriggerAt);

        waitUpdateJobs.add(job);

        if (triggerTask) {
            waitExecJobs.add(JobTaskConverter.INSTANCE.toJobTaskPrepare(partitionTask));
        }

    }

    private Long calculateNextTriggerTime(JobPartitionTaskDTO partitionTask, long now) {

        long nextTriggerAt = partitionTask.getNextTriggerAt();
        if ((nextTriggerAt + DateUtils.toEpochMilli(SystemConstants.SCHEDULE_PERIOD)) < now) {
            nextTriggerAt = now;
            partitionTask.setNextTriggerAt(nextTriggerAt);
        }

        // 更新下次触发时间
        WaitStrategy waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(partitionTask.getTriggerType().getValue());
        WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();
        waitStrategyContext.setTriggerInterval(partitionTask.getTriggerInterval());
        waitStrategyContext.setNextTriggerAt(nextTriggerAt);

        return waitStrategy.computeTriggerTime(waitStrategyContext);
    }

    private List<JobPartitionTaskDTO> listAvailableJobs(Long startId, ScanTask scanTask) {
        if (CollectionUtils.isEmpty(scanTask.getBuckets())) {
            return Collections.emptyList();
        }

        List<Job> jobs = jobDao.selectPage(new PageDTO<>(0, systemProperties.getJobPullPageSize()),
                new LambdaQueryWrapper<Job>()
                        .select(Job::getGroupName, Job::getNextTriggerAt, Job::getBlockStrategy, Job::getTriggerType,
                                Job::getTriggerInterval, Job::getExecutorTimeout, Job::getTaskType, Job::getResident,
                                Job::getId, Job::getNamespaceId)
                        .eq(Job::getJobStatus, true)
                        .ne(Job::getTriggerType, SystemConstants.WORKFLOW_TRIGGER_TYPE)
                        .in(Job::getBucketIndex, scanTask.getBuckets())
                        .le(Job::getNextTriggerAt,
                                DateUtils.toNowMilli() + DateUtils.toEpochMilli(SystemConstants.SCHEDULE_PERIOD))
                        .ge(Job::getId, startId)
                        .orderByAsc(Job::getId)
        ).getRecords();

        // 过滤已关闭的组
        if (CollectionUtils.isNotEmpty(jobs)) {
            List<String> groupConfigs = StreamUtils.toList(groupConfigDao.selectList(new LambdaQueryWrapper<GroupConfig>()
                            .select(GroupConfig::getGroupName)
                            .eq(GroupConfig::getGroupStatus, true)
                            .in(GroupConfig::getGroupName, StreamUtils.toSet(jobs, Job::getGroupName))),
                    GroupConfig::getGroupName);
            jobs = jobs.stream().filter(job -> groupConfigs.contains(job.getGroupName())).collect(Collectors.toList());
        }

        return CollectionUtils.transformToList(jobs, JobTaskConverter.INSTANCE::toJobPartitionTask);
    }
}
