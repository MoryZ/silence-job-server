package com.old.silence.job.server.job.task.support.block.workflow;

import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.server.job.task.support.WorkflowTaskConverter;
import com.old.silence.job.server.job.task.support.generator.batch.WorkflowBatchGenerator;
import com.old.silence.job.server.job.task.support.generator.batch.WorkflowTaskBatchGeneratorContext;
import com.old.silence.job.server.job.task.support.handler.WorkflowBatchHandler;


@Component
public class DiscardWorkflowBlockStrategy extends AbstractWorkflowBlockStrategy {
    private final WorkflowBatchGenerator workflowBatchGenerator;
    private final WorkflowBatchHandler workflowBatchHandler;

    public DiscardWorkflowBlockStrategy(WorkflowBatchGenerator workflowBatchGenerator, WorkflowBatchHandler workflowBatchHandler) {
        this.workflowBatchGenerator = workflowBatchGenerator;
        this.workflowBatchHandler = workflowBatchHandler;
    }

    @Override
    protected void doBlock(WorkflowBlockStrategyContext workflowBlockStrategyContext) {

//        try {
//            workflowBatchHandler.recoveryWorkflowExecutor(workflowBlockStrategyContext.getWorkflowTaskBatchId(), null);
//        } catch (IOException e) {
//            throw new SilenceJobServerException("校验工作流失败", e);
//        }
        // 生成状态为取消的工作流批次
        WorkflowTaskBatchGeneratorContext workflowTaskBatchGeneratorContext = WorkflowTaskConverter.INSTANCE.toWorkflowTaskBatchGeneratorContext(workflowBlockStrategyContext);
        workflowTaskBatchGeneratorContext.setTaskBatchStatus(JobTaskBatchStatus.CANCEL);
        workflowTaskBatchGeneratorContext.setOperationReason(JobOperationReason.JOB_DISCARD);
        workflowBatchGenerator.generateJobTaskBatch(workflowTaskBatchGeneratorContext);
    }

    @Override
    protected JobBlockStrategy blockStrategyEnum() {
        return JobBlockStrategy.DISCARD;
    }
}
