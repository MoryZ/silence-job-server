package com.old.silence.job.server.job.task.support.block.workflow;

import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.server.job.task.support.WorkflowTaskConverter;
import com.old.silence.job.server.job.task.support.generator.batch.WorkflowBatchGenerator;
import com.old.silence.job.server.job.task.support.generator.batch.WorkflowTaskBatchGeneratorContext;
import com.old.silence.job.server.job.task.support.handler.WorkflowBatchHandler;


@Component
public class ConcurrencyWorkflowBlockStrategy extends AbstractWorkflowBlockStrategy {
    private final WorkflowBatchGenerator workflowBatchGenerator;
    private final WorkflowBatchHandler workflowBatchHandler;

    public ConcurrencyWorkflowBlockStrategy(WorkflowBatchGenerator workflowBatchGenerator, WorkflowBatchHandler workflowBatchHandler) {
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

        WorkflowTaskBatchGeneratorContext workflowTaskBatchGeneratorContext = WorkflowTaskConverter.INSTANCE.toWorkflowTaskBatchGeneratorContext(workflowBlockStrategyContext);
        workflowBatchGenerator.generateJobTaskBatch(workflowTaskBatchGeneratorContext);
    }

    @Override
    protected JobBlockStrategy blockStrategyEnum() {
        return JobBlockStrategy.CONCURRENCY;
    }
}
