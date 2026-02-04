package com.old.silence.job.server.retry.task.support.block;

import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.RetryBlockStrategy;
import com.old.silence.job.common.enums.RetryOperationReason;
import com.old.silence.job.common.enums.RetryTaskStatus;
import com.old.silence.job.server.retry.task.dto.RetryTaskGeneratorDTO;
import com.old.silence.job.server.retry.task.dto.TaskStopJobDTO;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;
import com.old.silence.job.server.retry.task.support.generator.task.RetryTaskGeneratorHandler;
import com.old.silence.job.server.retry.task.support.handler.RetryTaskStopHandler;

import java.util.Objects;


@Component
public class OverlayRetryBlockStrategy extends AbstracJobBlockStrategy {
    private final RetryTaskGeneratorHandler retryTaskGeneratorHandler;
    private final RetryTaskStopHandler retryTaskStopHandler;

    public OverlayRetryBlockStrategy(RetryTaskGeneratorHandler retryTaskGeneratorHandler, RetryTaskStopHandler retryTaskStopHandler) {
        this.retryTaskGeneratorHandler = retryTaskGeneratorHandler;
        this.retryTaskStopHandler = retryTaskStopHandler;
    }

    @Override
    public void doBlock(BlockStrategyContext context) {

        // 重新生成任务
        RetryTaskGeneratorDTO generatorDTO = RetryTaskConverter.INSTANCE.toRetryTaskGeneratorDTO(context);
        generatorDTO.setTaskStatus(RetryTaskStatus.CANCEL);
        generatorDTO.setOperationReason(RetryOperationReason.RETRY_TASK_DISCARD);
        retryTaskGeneratorHandler.generateRetryTask(generatorDTO);

        TaskStopJobDTO stopJobDTO = RetryTaskConverter.INSTANCE.toTaskStopJobDTO(context);
        if (Objects.isNull(context.getOperationReason()) || context.getOperationReason().equals(JobOperationReason.NONE)) {

            stopJobDTO.setOperationReason(RetryOperationReason.RETRY_TASK_OVERLAY);
        }

        stopJobDTO.setNeedUpdateTaskStatus(true);
        retryTaskStopHandler.stop(stopJobDTO);

    }

    @Override
    protected RetryBlockStrategy blockStrategyEnum() {
        return RetryBlockStrategy.OVERLAY;
    }
}
