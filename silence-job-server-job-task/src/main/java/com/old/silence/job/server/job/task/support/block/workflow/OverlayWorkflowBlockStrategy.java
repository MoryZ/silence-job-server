package com.old.silence.job.server.job.task.support.block.workflow;

import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.server.job.task.support.WorkflowTaskConverter;
import com.old.silence.job.server.job.task.support.generator.batch.WorkflowBatchGenerator;
import com.old.silence.job.server.job.task.support.generator.batch.WorkflowTaskBatchGeneratorContext;
import com.old.silence.job.server.job.task.support.handler.WorkflowBatchHandler;


@Component
public class OverlayWorkflowBlockStrategy extends AbstractWorkflowBlockStrategy {

    private final WorkflowBatchHandler workflowBatchHandler;
    private final WorkflowBatchGenerator workflowBatchGenerator;

    public OverlayWorkflowBlockStrategy(WorkflowBatchHandler workflowBatchHandler, WorkflowBatchGenerator workflowBatchGenerator) {
        this.workflowBatchHandler = workflowBatchHandler;
        this.workflowBatchGenerator = workflowBatchGenerator;
    }

    @Override
    protected void doBlock(WorkflowBlockStrategyContext workflowBlockStrategyContext) {

        // 停止当前批次
        workflowBatchHandler.stop(workflowBlockStrategyContext.getWorkflowTaskBatchId(), workflowBlockStrategyContext.getOperationReason());

        // 重新生成一个批次
        WorkflowTaskBatchGeneratorContext workflowTaskBatchGeneratorContext = WorkflowTaskConverter.INSTANCE.toWorkflowTaskBatchGeneratorContext(
                workflowBlockStrategyContext);
        workflowBatchGenerator.generateJobTaskBatch(workflowTaskBatchGeneratorContext);

    }

    @Override
    protected JobBlockStrategy blockStrategyEnum() {
        return JobBlockStrategy.OVERLAY;
    }
}
