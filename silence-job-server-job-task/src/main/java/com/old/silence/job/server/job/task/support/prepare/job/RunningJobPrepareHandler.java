package com.old.silence.job.server.job.task.support.prepare.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.job.task.dto.CompleteJobBatchDTO;
import com.old.silence.job.server.job.task.dto.JobTaskFailAlarmEventDTO;
import com.old.silence.job.server.job.task.dto.JobTaskPrepareDTO;
import com.old.silence.job.server.job.task.support.BlockStrategy;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.job.task.support.JobTaskStopHandler;
import com.old.silence.job.server.job.task.support.block.job.BlockStrategyContext;
import com.old.silence.job.server.job.task.support.block.job.JobBlockStrategyFactory;
import com.old.silence.job.server.job.task.support.handler.JobTaskBatchHandler;
import com.old.silence.job.server.job.task.support.stop.JobTaskStopFactory;
import com.old.silence.job.server.job.task.support.stop.TaskStopJobContext;

/**
 * 处理处于{@link JobTaskBatchStatus ::RUNNING}状态的任务
 */
@Component

public class RunningJobPrepareHandler extends AbstractJobPrepareHandler {


    private static final Logger log = LoggerFactory.getLogger(RunningJobPrepareHandler.class);
    private final JobTaskBatchHandler jobTaskBatchHandler;

    public RunningJobPrepareHandler(JobTaskBatchHandler jobTaskBatchHandler) {
        this.jobTaskBatchHandler = jobTaskBatchHandler;
    }

    @Override
    public boolean matches(JobTaskBatchStatus status) {
        return JobTaskBatchStatus.RUNNING.equals(status);
    }

    @Override
    protected void  doHandle(JobTaskPrepareDTO prepare) {
        log.debug("存在运行中的任务. prepare:[{}]", JSON.toJSONString(prepare));

        // 若存在所有的任务都是完成，但是批次上的状态为运行中，则是并发导致的未把批次状态变成为终态，此处做一次兜底处理
        JobBlockStrategy blockStrategy = prepare.getBlockStrategy();
        JobOperationReason jobOperationReason = JobOperationReason.NONE;
        CompleteJobBatchDTO completeJobBatchDTO = JobTaskConverter.INSTANCE.completeJobBatchDTO(prepare);
        completeJobBatchDTO.setJobOperationReason(jobOperationReason);
        completeJobBatchDTO.setRetryStatus(Boolean.FALSE);
        if (jobTaskBatchHandler.handleResult(completeJobBatchDTO)) {
            blockStrategy = JobBlockStrategy.CONCURRENCY;
        } else {
            // 计算超时时间
            long delay = DateUtils.toNowMilli() - prepare.getExecutionAt();

            // 计算超时时间，到达超时时间中断任务
            if (delay > DateUtils.toEpochMilli(prepare.getExecutorTimeout())) {
                log.info("任务执行超时.taskBatchId:[{}] delay:[{}] executorTimeout:[{}]", prepare.getTaskBatchId(), delay, DateUtils.toEpochMilli(prepare.getExecutorTimeout()));
                // 超时停止任务
                JobTaskStopHandler instanceInterrupt = JobTaskStopFactory.getJobTaskStop(prepare.getTaskType());
                TaskStopJobContext stopJobContext = JobTaskConverter.INSTANCE.toStopJobContext(prepare);
                stopJobContext.setJobOperationReason(JobOperationReason.TASK_EXECUTION_TIMEOUT);
                stopJobContext.setNeedUpdateTaskStatus(Boolean.TRUE);
                instanceInterrupt.stop(stopJobContext);

                var jobTaskFailAlarmEventDTO = new JobTaskFailAlarmEventDTO();
                jobTaskFailAlarmEventDTO.setJobTaskBatchId(prepare.getTaskBatchId());
                SilenceSpringContext.getContext().publishEvent(jobTaskFailAlarmEventDTO);
            }
        }

        // 仅是超时检测的，不执行阻塞策略
        if (prepare.isOnlyTimeoutCheck()) {
            return;
        }

        BlockStrategyContext blockStrategyContext = JobTaskConverter.INSTANCE.toBlockStrategyContext(prepare);
        blockStrategyContext.setOperationReason(jobOperationReason);
        BlockStrategy blockStrategyInterface = JobBlockStrategyFactory.getBlockStrategy(blockStrategy);
        blockStrategyInterface.block(blockStrategyContext);

    }

}
