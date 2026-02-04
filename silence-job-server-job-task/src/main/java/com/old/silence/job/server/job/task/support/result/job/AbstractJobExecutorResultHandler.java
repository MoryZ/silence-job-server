package com.old.silence.job.server.job.task.support.result.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.common.enums.JobTaskExecutorScene;
import com.old.silence.job.common.enums.JobTaskStatus;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.infrastructure.persistence.dao.GroupConfigDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.job.task.dto.JobTaskFailAlarmEventDTO;
import com.old.silence.job.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.old.silence.job.server.job.task.support.JobExecutorResultHandler;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.job.task.support.JobTaskStopHandler;
import com.old.silence.job.server.job.task.support.alarm.event.JobTaskFailAlarmEvent;
import com.old.silence.job.server.job.task.support.handler.WorkflowBatchHandler;
import com.old.silence.job.server.job.task.support.stop.JobTaskStopFactory;
import com.old.silence.job.server.job.task.support.stop.TaskStopJobContext;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public abstract class AbstractJobExecutorResultHandler implements JobExecutorResultHandler {

    private final JobTaskDao jobTaskDao;
    private final JobTaskBatchDao jobTaskBatchDao;
    private final WorkflowBatchHandler workflowBatchHandler;
    private final GroupConfigDao groupConfigDao;

    protected AbstractJobExecutorResultHandler(JobTaskDao jobTaskDao, JobTaskBatchDao jobTaskBatchDao, WorkflowBatchHandler workflowBatchHandler, GroupConfigDao groupConfigDao) {
        this.jobTaskDao = jobTaskDao;
        this.jobTaskBatchDao = jobTaskBatchDao;
        this.workflowBatchHandler = workflowBatchHandler;
        this.groupConfigDao = groupConfigDao;
    }

    @Override
    public void handleResult(JobExecutorResultContext context) {

        List<JobTask> jobTasks = jobTaskDao.selectList(
                new LambdaQueryWrapper<JobTask>()
                        .select(JobTask::getTaskStatus, JobTask::getMrStage)
                        .eq(JobTask::getTaskBatchId, context.getTaskBatchId()));

        if (CollectionUtils.isEmpty(jobTasks) ||
                jobTasks.stream().anyMatch(jobTask -> JobTaskStatus.NOT_COMPLETE.contains(jobTask.getTaskStatus()))) {
            return;
        }

        // 放入任务项, 子类会用到
        context.setJobTaskList(jobTasks);

        Map<JobTaskStatus, Long> statusCountMap = jobTasks.stream()
                .collect(Collectors.groupingBy(JobTask::getTaskStatus, Collectors.counting()));

        long failCount = statusCountMap.getOrDefault(JobTaskStatus.FAIL, 0L);
        long stopCount = statusCountMap.getOrDefault(JobTaskStatus.STOP, 0L);

        JobTaskBatchStatus taskBatchStatus;
        if (failCount > 0) {
            taskBatchStatus = JobTaskBatchStatus.FAIL;

            var jobTaskFailAlarmEventDTO = new JobTaskFailAlarmEventDTO();

            jobTaskFailAlarmEventDTO.setJobTaskBatchId(context.getTaskBatchId());
            jobTaskFailAlarmEventDTO.setReason(context.getMessage());
            jobTaskFailAlarmEventDTO.setNotifyScene(JobNotifyScene.JOB_TASK_ERROR);

            SilenceSpringContext.getContext().publishEvent(
                    new JobTaskFailAlarmEvent(jobTaskFailAlarmEventDTO));
            doHandleFail(context);
        } else if (stopCount > 0) {
            taskBatchStatus = JobTaskBatchStatus.STOP;
            doHandleStop(context);
        } else {
            taskBatchStatus = JobTaskBatchStatus.SUCCESS;
            doHandleSuccess(context);
        }

        // 开启下一个工作流节点
        openNextWorkflowNode(context);

        boolean res = updateStatus(context, taskBatchStatus);
        context.setTaskBatchComplete(res);
        if (res) {
            // 停止客户端的任务
            stop(context);
        }
    }

    protected void openNextWorkflowNode(JobExecutorResultContext context) {
        WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
        taskExecuteDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
        taskExecuteDTO.setTaskExecutorScene(JobTaskExecutorScene.AUTO_WORKFLOW);
        taskExecuteDTO.setParentId(context.getWorkflowNodeId());
        taskExecuteDTO.setTaskBatchId(context.getTaskBatchId());
        workflowBatchHandler.openNextNode(taskExecuteDTO);
    }

    protected boolean updateStatus(JobExecutorResultContext context, JobTaskBatchStatus taskBatchStatus) {
        JobTaskBatch jobTaskBatch = new JobTaskBatch();
        jobTaskBatch.setId(context.getTaskBatchId());
        jobTaskBatch.setTaskBatchStatus(taskBatchStatus);
        jobTaskBatch.setUpdatedDate(Instant.now());
        jobTaskBatch.setOperationReason(
                Optional.ofNullable(context.getJobOperationReason()).orElse(JobOperationReason.NONE)
        );

        if (JobTaskBatchStatus.NOT_SUCCESS.contains(taskBatchStatus) && context.isRetry()) {
            jobTaskBatchDao.update(jobTaskBatch,
                    new LambdaUpdateWrapper<JobTaskBatch>()
                            .eq(JobTaskBatch::getId, context.getTaskBatchId()));
            return false;
        }

        return 1 == jobTaskBatchDao.update(jobTaskBatch,
                new LambdaUpdateWrapper<JobTaskBatch>()
                        .eq(JobTaskBatch::getId, context.getTaskBatchId())
                        .in(!context.isRetry(), JobTaskBatch::getTaskBatchStatus, JobTaskBatchStatus.NOT_COMPLETE)
        );
    }

    protected void stop(JobExecutorResultContext context) {
        JobTaskStopHandler instanceInterrupt = JobTaskStopFactory.getJobTaskStop(getTaskInstanceType());
        TaskStopJobContext stopJobContext = JobTaskConverter.INSTANCE.toStopJobContext(context);
        stopJobContext.setNeedUpdateTaskStatus(Boolean.FALSE);
        stopJobContext.setForceStop(Boolean.TRUE);
        instanceInterrupt.stop(stopJobContext);
    }

    protected abstract void doHandleSuccess(JobExecutorResultContext context);

    protected abstract void doHandleStop(JobExecutorResultContext context);

    protected abstract void doHandleFail(JobExecutorResultContext context);

}
