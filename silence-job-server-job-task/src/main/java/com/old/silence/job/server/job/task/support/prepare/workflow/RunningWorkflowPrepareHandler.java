package com.old.silence.job.server.job.task.support.prepare.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.job.task.dto.WorkflowTaskFailAlarmEventDTO;
import com.old.silence.job.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.old.silence.job.server.job.task.support.BlockStrategy;
import com.old.silence.job.server.job.task.support.WorkflowTaskConverter;
import com.old.silence.job.server.job.task.support.alarm.event.WorkflowTaskFailAlarmEvent;
import com.old.silence.job.server.job.task.support.block.workflow.WorkflowBlockStrategyContext;
import com.old.silence.job.server.job.task.support.block.workflow.WorkflowBlockStrategyFactory;
import com.old.silence.job.server.job.task.support.handler.WorkflowBatchHandler;

import java.util.Objects;


@Component
public class RunningWorkflowPrepareHandler extends AbstractWorkflowPrePareHandler {


    private static final Logger log = LoggerFactory.getLogger(RunningWorkflowPrepareHandler.class);
    private final WorkflowBatchHandler workflowBatchHandler;

    public RunningWorkflowPrepareHandler(WorkflowBatchHandler workflowBatchHandler) {
        this.workflowBatchHandler = workflowBatchHandler;
    }

    @Override
    public boolean matches(JobTaskBatchStatus status) {
        return Objects.nonNull(status) && JobTaskBatchStatus.RUNNING == status;
    }

    @Override
    protected void doHandler(WorkflowTaskPrepareDTO prepare) {
        log.debug("存在运行中的任务. prepare:[{}]", JSON.toJSONString(prepare));


        // 1. 若DAG已经支持完成了，由于异常原因导致的没有更新成终态此次进行一次更新操作
        JobBlockStrategy blockStrategy = prepare.getBlockStrategy();
        if (workflowBatchHandler.complete(prepare.getWorkflowTaskBatchId())) {
            // 开启新的任务
            blockStrategy = JobBlockStrategy.CONCURRENCY;
        } else {
            // 计算超时时间
            long delay = DateUtils.toNowMilli() - prepare.getExecutionAt();

            // 2. 判断DAG是否已经支持超时
            // 计算超时时间，到达超时时间中断任务
            if (delay > DateUtils.toEpochMilli(prepare.getExecutorTimeout())) {

                // 超时停止任务
                String reason = String.format("任务执行超时.workflowTaskBatchId:[%s] delay:[%s] executorTimeout:[%s]", prepare.getWorkflowTaskBatchId(), delay, DateUtils.toEpochMilli(prepare.getExecutorTimeout()));
                var workflowTaskFailAlarmEventDTO = new WorkflowTaskFailAlarmEventDTO();
                workflowTaskFailAlarmEventDTO.setWorkflowTaskBatchId(prepare.getWorkflowTaskBatchId());
                workflowTaskFailAlarmEventDTO.setReason(reason);
                workflowTaskFailAlarmEventDTO.setNotifyScene(JobNotifyScene.WORKFLOW_TASK_ERROR);

                SilenceSpringContext.getContext().publishEvent(new WorkflowTaskFailAlarmEvent(workflowTaskFailAlarmEventDTO));
                log.info(reason);
            }
        }

        // 仅是超时检测的，不执行阻塞策略
        if (prepare.isOnlyTimeoutCheck()) {
            return;
        }

        // 3. 支持阻塞策略同JOB逻辑一致
        BlockStrategy blockStrategyInterface = WorkflowBlockStrategyFactory.getBlockStrategy(blockStrategy);
        WorkflowBlockStrategyContext workflowBlockStrategyContext = WorkflowTaskConverter.INSTANCE.toWorkflowBlockStrategyContext(
                prepare);
        blockStrategyInterface.block(workflowBlockStrategyContext);

    }
}
