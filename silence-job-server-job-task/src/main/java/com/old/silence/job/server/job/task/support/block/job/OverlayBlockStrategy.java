package com.old.silence.job.server.job.task.support.block.job;

import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.job.task.support.JobTaskStopHandler;
import com.old.silence.job.server.job.task.support.generator.batch.JobTaskBatchGenerator;
import com.old.silence.job.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;
import com.old.silence.job.server.job.task.support.stop.JobTaskStopFactory;
import com.old.silence.job.server.job.task.support.stop.TaskStopJobContext;

import java.util.Objects;


@Component
public class OverlayBlockStrategy extends AbstracJobBlockStrategy {
    private final JobTaskBatchGenerator jobTaskBatchGenerator;

    public OverlayBlockStrategy(JobTaskBatchGenerator jobTaskBatchGenerator) {
        this.jobTaskBatchGenerator = jobTaskBatchGenerator;
    }

    @Override
    public void doBlock(BlockStrategyContext context) {

        // 重新生成任务
        JobTaskBatchGeneratorContext jobTaskBatchGeneratorContext = JobTaskConverter.INSTANCE.toJobTaskGeneratorContext(context);
        jobTaskBatchGenerator.generateJobTaskBatch(jobTaskBatchGeneratorContext);

        // 停止任务
        JobTaskStopHandler instanceInterrupt = JobTaskStopFactory.getJobTaskStop(context.getTaskType());
        TaskStopJobContext stopJobContext = JobTaskConverter.INSTANCE.toStopJobContext(context);

        JobOperationReason operationReason = context.getOperationReason();
        if (Objects.isNull(context.getOperationReason()) || context.getOperationReason().equals(JobOperationReason.NONE)) {

            operationReason = JobOperationReason.JOB_OVERLAY;
        }

        stopJobContext.setJobOperationReason(operationReason);
        stopJobContext.setNeedUpdateTaskStatus(Boolean.TRUE);
        instanceInterrupt.stop(stopJobContext);
    }

    @Override
    protected JobBlockStrategy blockStrategyEnum() {
        return JobBlockStrategy.OVERLAY;
    }
}
