package com.old.silence.job.server.job.task.support.generator.batch;

import cn.hutool.core.lang.Assert;
import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.domain.model.WorkflowTaskBatch;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowTaskBatchDao;
import com.old.silence.job.server.job.task.dto.WorkflowTimerTaskDTO;
import com.old.silence.job.server.job.task.support.WorkflowTaskConverter;
import com.old.silence.job.server.job.task.support.timer.JobTimerWheel;
import com.old.silence.job.server.job.task.support.timer.WorkflowTimerTask;

import java.time.Duration;
import java.util.Optional;


@Component

public class WorkflowBatchGenerator {
    private final WorkflowTaskBatchDao workflowTaskBatchDao;

    public WorkflowBatchGenerator(WorkflowTaskBatchDao workflowTaskBatchDao) {
        this.workflowTaskBatchDao = workflowTaskBatchDao;
    }

    public void generateJobTaskBatch(WorkflowTaskBatchGeneratorContext context) {

        // 生成任务批次
        WorkflowTaskBatch workflowTaskBatch = WorkflowTaskConverter.INSTANCE.toWorkflowTaskBatch(context);
        workflowTaskBatch.setTaskBatchStatus(Optional.ofNullable(context.getTaskBatchStatus()).orElse(JobTaskBatchStatus.WAITING));
        workflowTaskBatch.setOperationReason(context.getOperationReason());
        workflowTaskBatch.setWfContext(context.getWfContext());

        Assert.isTrue(1 == workflowTaskBatchDao.insert(workflowTaskBatch), () -> new SilenceJobServerException("新增调度任务失败. [{}]", context.getWorkflowId()));

        // 非待处理状态无需进入时间轮中
        if (JobTaskBatchStatus.WAITING != workflowTaskBatch.getTaskBatchStatus()) {
            return;
        }

        // 开始执行工作流
        // 进入时间轮
        long delay = context.getNextTriggerAt().longValue() - DateUtils.toNowMilli();
        WorkflowTimerTaskDTO workflowTimerTaskDTO = new WorkflowTimerTaskDTO();
        workflowTimerTaskDTO.setWorkflowTaskBatchId(workflowTaskBatch.getId());
        workflowTimerTaskDTO.setWorkflowId(context.getWorkflowId());
        workflowTimerTaskDTO.setTaskExecutorScene(context.getTaskExecutorScene());

        JobTimerWheel.registerWithWorkflow(() -> new WorkflowTimerTask(workflowTimerTaskDTO), Duration.ofMillis(delay));
    }
}
