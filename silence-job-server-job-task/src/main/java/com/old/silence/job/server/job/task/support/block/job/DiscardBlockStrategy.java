package com.old.silence.job.server.job.task.support.block.job;

import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.job.task.support.generator.batch.JobTaskBatchGenerator;
import com.old.silence.job.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;


@Component
public class DiscardBlockStrategy extends AbstracJobBlockStrategy {
    private final JobTaskBatchGenerator jobTaskBatchGenerator;

    public DiscardBlockStrategy(JobTaskBatchGenerator jobTaskBatchGenerator) {
        this.jobTaskBatchGenerator = jobTaskBatchGenerator;
    }

    @Override
    public void doBlock(BlockStrategyContext context) {
        // 重新生成任务
        JobTaskBatchGeneratorContext jobTaskBatchGeneratorContext = JobTaskConverter.INSTANCE.toJobTaskGeneratorContext(context);
        jobTaskBatchGeneratorContext.setTaskBatchStatus(JobTaskBatchStatus.CANCEL);
        jobTaskBatchGeneratorContext.setOperationReason(JobOperationReason.JOB_DISCARD);
        jobTaskBatchGenerator.generateJobTaskBatch(jobTaskBatchGeneratorContext);
    }

    @Override
    protected JobBlockStrategy blockStrategyEnum() {
        return JobBlockStrategy.DISCARD;
    }
}
