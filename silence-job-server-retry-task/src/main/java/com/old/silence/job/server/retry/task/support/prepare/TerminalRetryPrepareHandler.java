package com.old.silence.job.server.retry.task.support.prepare;

import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.RetryTaskStatus;
import com.old.silence.job.server.retry.task.dto.RetryTaskPrepareDTO;
import com.old.silence.job.server.retry.task.support.RetryPrePareHandler;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;
import com.old.silence.job.server.retry.task.support.generator.task.RetryTaskGeneratorHandler;

import static com.old.silence.job.common.enums.RetryTaskStatus.TERMINAL_STATUS_SET;

@Component
public class TerminalRetryPrepareHandler implements RetryPrePareHandler {
    private final RetryTaskGeneratorHandler retryTaskGeneratorHandler;

    public TerminalRetryPrepareHandler(RetryTaskGeneratorHandler retryTaskGeneratorHandler) {
        this.retryTaskGeneratorHandler = retryTaskGeneratorHandler;
    }

    @Override
    public boolean matches(RetryTaskStatus status) {
        return TERMINAL_STATUS_SET.contains(status);
    }

    @Override
    public void handle(RetryTaskPrepareDTO jobPrepareDTO) {
        retryTaskGeneratorHandler.generateRetryTask(RetryTaskConverter.INSTANCE.toRetryTaskGeneratorDTO(jobPrepareDTO));
    }
}
