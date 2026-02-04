package com.old.silence.job.server.retry.task.support.prepare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.RetryBlockStrategy;
import com.old.silence.job.common.enums.RetryOperationReason;
import com.old.silence.job.common.enums.RetryTaskStatus;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.retry.task.dto.RetryTaskPrepareDTO;
import com.old.silence.job.server.retry.task.dto.TaskStopJobDTO;
import com.old.silence.job.server.retry.task.support.BlockStrategy;
import com.old.silence.job.server.retry.task.support.RetryPrePareHandler;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;
import com.old.silence.job.server.retry.task.support.block.BlockStrategyContext;
import com.old.silence.job.server.retry.task.support.block.RetryBlockStrategyFactory;
import com.old.silence.job.server.retry.task.support.handler.RetryTaskStopHandler;

import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 */
@Component
public class RunningRetryPrepareHandler implements RetryPrePareHandler {


    private static final Logger log = LoggerFactory.getLogger(RunningRetryPrepareHandler.class);
    private final RetryTaskStopHandler retryTaskStopHandler;

    public RunningRetryPrepareHandler(RetryTaskStopHandler retryTaskStopHandler) {
        this.retryTaskStopHandler = retryTaskStopHandler;
    }

    @Override
    public boolean matches(RetryTaskStatus status) {
        return Objects.equals(RetryTaskStatus.RUNNING, status);
    }

    @Override
    public void handle(RetryTaskPrepareDTO prepare) {
        // 若存在所有的任务都是完成，但是批次上的状态为运行中，则是并发导致的未把批次状态变成为终态，此处做一次兜底处理
        RetryBlockStrategy blockStrategy = prepare.getBlockStrategy();
        JobOperationReason jobOperationReason = JobOperationReason.NONE;

        // 计算超时时间
        long delay = DateUtils.toNowMilli() - prepare.getNextTriggerAt();

        // 计算超时时间，到达超时时间中断任务
        if (delay > DateUtils.toEpochMilli(prepare.getExecutorTimeout())) {
            log.info("任务执行超时.retryTaskId:[{}] delay:[{}] executorTimeout:[{}]", prepare.getRetryTaskId(), delay, DateUtils.toEpochMilli(prepare.getExecutorTimeout()));
            // 超时停止任务
            TaskStopJobDTO stopJobDTO = RetryTaskConverter.INSTANCE.toTaskStopJobDTO(prepare);
            stopJobDTO.setOperationReason(RetryOperationReason.TASK_EXECUTION_TIMEOUT);
            stopJobDTO.setNeedUpdateTaskStatus(true);
            retryTaskStopHandler.stop(stopJobDTO);
        }

        // 仅是超时检测的，不执行阻塞策略
        if (prepare.isOnlyTimeoutCheck()) {
            return;
        }

        BlockStrategyContext blockStrategyContext = RetryTaskConverter.INSTANCE.toBlockStrategyContext(prepare);
        blockStrategyContext.setOperationReason(jobOperationReason);
        BlockStrategy blockStrategyInterface = RetryBlockStrategyFactory.getBlockStrategy(blockStrategy);
        blockStrategyInterface.block(blockStrategyContext);

    }
}
