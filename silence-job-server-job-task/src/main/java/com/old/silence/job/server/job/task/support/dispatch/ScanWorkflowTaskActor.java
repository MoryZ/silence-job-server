package com.old.silence.job.server.job.task.support.dispatch;

import cn.hutool.core.util.RandomUtil;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
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
import com.old.silence.job.server.domain.model.Workflow;
import com.old.silence.job.server.infrastructure.persistence.dao.GroupConfigDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowDao;
import com.old.silence.job.server.job.task.dto.WorkflowPartitionTaskDTO;
import com.old.silence.job.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.old.silence.job.server.job.task.support.WorkflowTaskConverter;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Component(ActorGenerator.SCAN_WORKFLOW_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)

public class ScanWorkflowTaskActor extends AbstractActor {
    private final WorkflowDao workflowDao;
    private final SystemProperties systemProperties;
    private final GroupConfigDao groupConfigDao;

    public ScanWorkflowTaskActor(WorkflowDao workflowDao, SystemProperties systemProperties,
                                 GroupConfigDao groupConfigDao) {
        this.workflowDao = workflowDao;
        this.systemProperties = systemProperties;
        this.groupConfigDao = groupConfigDao;
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

    private void doScan(ScanTask scanTask) {
        PartitionTaskUtils.process(startId -> listAvailableWorkflows(startId, scanTask),
                this::processPartitionTasks, 0);
    }

    private void processPartitionTasks(List<? extends PartitionTask> partitionTasks) {
        List<Workflow> waitUpdateJobs = new ArrayList<>();
        List<WorkflowTaskPrepareDTO> waitExecWorkflows = new ArrayList<>();
        long now = DateUtils.toNowMilli();
        for (PartitionTask partitionTask : partitionTasks) {
            WorkflowPartitionTaskDTO workflowPartitionTaskDTO = (WorkflowPartitionTaskDTO) partitionTask;
            processWorkflow(workflowPartitionTaskDTO, waitUpdateJobs, waitExecWorkflows, now);
        }

        // 批量更新
        workflowDao.updateBatchNextTriggerAtById(waitUpdateJobs);

        for (final WorkflowTaskPrepareDTO waitExecTask : waitExecWorkflows) {
            // 执行预处理阶段
            ActorRef actorRef = ActorGenerator.workflowTaskPrepareActor();
            waitExecTask.setTaskExecutorScene(JobTaskExecutorScene.AUTO_WORKFLOW);
            actorRef.tell(waitExecTask, actorRef);
        }
    }

    private void processWorkflow(WorkflowPartitionTaskDTO partitionTask, List<Workflow> waitUpdateWorkflows,
                                 List<WorkflowTaskPrepareDTO> waitExecJobs, long now) {
        CacheConsumerGroup.addOrUpdate(partitionTask.getGroupName(), partitionTask.getNamespaceId());

        // 更新下次触发时间
        Long nextTriggerAt = calculateNextTriggerTime(partitionTask, now);

        Workflow workflow = new Workflow();
        workflow.setId(partitionTask.getId());
        workflow.setNextTriggerAt(nextTriggerAt);
        waitUpdateWorkflows.add(workflow);

        waitExecJobs.add(WorkflowTaskConverter.INSTANCE.toWorkflowTaskPrepareDTO(partitionTask));

    }

    private Long calculateNextTriggerTime(WorkflowPartitionTaskDTO partitionTask, long now) {

        long nextTriggerAt = partitionTask.getNextTriggerAt();
        if ((nextTriggerAt + DateUtils.toEpochMilli(SystemConstants.SCHEDULE_PERIOD)) < now) {
            long randomMs = (long) (RandomUtil.randomDouble(0, 4, 2, RoundingMode.UP) * 1000);
            nextTriggerAt = now + randomMs;
            partitionTask.setNextTriggerAt(nextTriggerAt);
        }

        // 更新下次触发时间
        WaitStrategy waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(partitionTask.getTriggerType().getValue());
        WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();
        waitStrategyContext.setTriggerInterval(partitionTask.getTriggerInterval());
        waitStrategyContext.setNextTriggerAt(nextTriggerAt);

        return waitStrategy.computeTriggerTime(waitStrategyContext);
    }

    private List<WorkflowPartitionTaskDTO> listAvailableWorkflows(Long startId, ScanTask scanTask) {
        if (CollectionUtils.isEmpty(scanTask.getBuckets())) {
            return Collections.emptyList();
        }

        List<Workflow> workflows = workflowDao.selectPage(new PageDTO<>(0, systemProperties.getJobPullPageSize()),
                new LambdaQueryWrapper<Workflow>()
                        .select(Workflow::getId, Workflow::getGroupName, Workflow::getNextTriggerAt, Workflow::getTriggerType,
                                Workflow::getTriggerInterval, Workflow::getExecutorTimeout, Workflow::getNamespaceId,
                                Workflow::getFlowInfo, Workflow::getBlockStrategy, Workflow::getWfContext)
                        .eq(Workflow::getWorkflowStatus, true)
                        .in(Workflow::getBucketIndex, scanTask.getBuckets())
                        .le(Workflow::getNextTriggerAt, DateUtils.toNowMilli() + DateUtils.toEpochMilli(SystemConstants.SCHEDULE_PERIOD))
                        .ge(Workflow::getId, startId)
                        .orderByAsc(Workflow::getId)
        ).getRecords();

        // 过滤已关闭的组
        if (CollectionUtils.isNotEmpty(workflows)) {
            List<String> groupConfigs = StreamUtils.toList(groupConfigDao.selectList(new LambdaQueryWrapper<GroupConfig>()
                            .select(GroupConfig::getGroupName)
                            .eq(GroupConfig::getGroupStatus, true)
                            .in(GroupConfig::getGroupName, StreamUtils.toSet(workflows, Workflow::getGroupName))),
                    GroupConfig::getGroupName);
            workflows = workflows.stream().filter(workflow -> groupConfigs.contains(workflow.getGroupName())).collect(Collectors.toList());
        }

        return CollectionUtils.transformToList(workflows, WorkflowTaskConverter.INSTANCE::toWorkflowPartitionTask);
    }
}
