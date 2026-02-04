package com.old.silence.job.server.job.task.support.dispatch;

import org.apache.pekko.actor.AbstractActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.common.enums.JobTaskExecutorScene;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.job.task.dto.JobTaskPrepareDTO;
import com.old.silence.job.server.job.task.support.JobPrepareHandler;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.util.List;

import static com.old.silence.job.common.enums.JobTaskBatchStatus.NOT_COMPLETE;

/**
 * 调度任务准备阶段
 */
@Component(ActorGenerator.JOB_TASK_PREPARE_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)

public class JobTaskPrepareActor extends AbstractActor {

    private static final Logger log = LoggerFactory.getLogger(JobTaskPrepareActor.class);
    private final JobTaskBatchDao jobTaskBatchDao;
    private final List<JobPrepareHandler> prepareHandlers;

    public JobTaskPrepareActor(JobTaskBatchDao jobTaskBatchDao,
                               List<JobPrepareHandler> prepareHandlers) {
        this.jobTaskBatchDao = jobTaskBatchDao;
        this.prepareHandlers = prepareHandlers;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(JobTaskPrepareDTO.class, job -> {
            try {
                doPrepare(job);
            } catch (Exception e) {
                log.error("预处理节点异常", e);
            } finally {
                getContext().stop(getSelf());
            }
        }).build();
    }

    private void doPrepare(JobTaskPrepareDTO prepare) {
        LambdaQueryWrapper<JobTaskBatch> queryWrapper = new LambdaQueryWrapper<JobTaskBatch>()
                .eq(JobTaskBatch::getJobId, prepare.getJobId())
                .in(JobTaskBatch::getTaskBatchStatus, NOT_COMPLETE);

        JobTaskExecutorScene jobTaskExecutorScene = prepare.getTaskExecutorScene();
        if (SystemTaskType.WORKFLOW.equals(jobTaskExecutorScene.getSystemTaskType())) {
            queryWrapper.eq(JobTaskBatch::getWorkflowNodeId, prepare.getWorkflowNodeId());
            queryWrapper.eq(JobTaskBatch::getWorkflowTaskBatchId, prepare.getWorkflowTaskBatchId());
            queryWrapper.eq(JobTaskBatch::getSystemTaskType, SystemTaskType.WORKFLOW.getValue());
        } else {
            queryWrapper.eq(JobTaskBatch::getSystemTaskType, SystemTaskType.JOB.getValue());
        }

        List<JobTaskBatch> notCompleteJobTaskBatchList = jobTaskBatchDao
                .selectList(queryWrapper);

        // 说明所以任务已经完成
        if (CollectionUtils.isEmpty(notCompleteJobTaskBatchList)) {
            JobTaskBatch jobTaskBatch = new JobTaskBatch();
            // 模拟完成情况
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatus.SUCCESS);
            notCompleteJobTaskBatchList = List.of(jobTaskBatch);
        }

        boolean onlyTimeoutCheck = false;
        for (JobTaskBatch jobTaskBatch : notCompleteJobTaskBatchList) {
            prepare.setExecutionAt(jobTaskBatch.getExecutionAt());
            prepare.setTaskBatchId(jobTaskBatch.getId());
            prepare.setOnlyTimeoutCheck(onlyTimeoutCheck);
            for (JobPrepareHandler prepareHandler : prepareHandlers) {
                if (prepareHandler.matches(jobTaskBatch.getTaskBatchStatus())) {
                    prepareHandler.handle(prepare);
                    break;
                }
            }

            // 当存在大量待处理任务时，除了第一个任务需要执行阻塞策略，其他任务只做任务检查
            onlyTimeoutCheck = true;
        }
    }
}
