package com.old.silence.job.server.retry.task.support.block;

import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.RetryBlockStrategy;
import com.old.silence.job.common.enums.RetryOperationReason;
import com.old.silence.job.common.enums.RetryTaskStatus;
import com.old.silence.job.server.retry.task.dto.RetryTaskGeneratorDTO;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;
import com.old.silence.job.server.retry.task.support.generator.task.RetryTaskGeneratorHandler;


@Component
public class DiscardRetryBlockStrategy extends AbstracJobBlockStrategy {
    private final RetryTaskGeneratorHandler retryTaskGeneratorHandler;

    public DiscardRetryBlockStrategy(RetryTaskGeneratorHandler retryTaskGeneratorHandler) {
        this.retryTaskGeneratorHandler = retryTaskGeneratorHandler;
    }

    @Override
    public void doBlock(final BlockStrategyContext context) {
        // 重新生成任务
        RetryTaskGeneratorDTO generatorDTO = RetryTaskConverter.INSTANCE.toRetryTaskGeneratorDTO(context);
        generatorDTO.setTaskStatus(RetryTaskStatus.CANCEL);
        generatorDTO.setOperationReason(RetryOperationReason.RETRY_TASK_DISCARD);
        retryTaskGeneratorHandler.generateRetryTask(generatorDTO);
    }

    @Override
    protected RetryBlockStrategy blockStrategyEnum() {
        return RetryBlockStrategy.DISCARD;
    }
}
