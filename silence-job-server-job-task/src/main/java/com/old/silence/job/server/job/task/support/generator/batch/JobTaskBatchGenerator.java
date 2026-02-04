package com.old.silence.job.server.job.task.support.generator.batch;

import cn.hutool.core.lang.Assert;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.common.enums.JobTaskExecutorScene;
import com.old.silence.job.server.common.cache.CacheRegisterTable;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.job.task.dto.JobTaskFailAlarmEventDTO;
import com.old.silence.job.server.job.task.dto.JobTimerTaskDTO;
import com.old.silence.job.server.job.task.dto.TaskExecuteDTO;
import com.old.silence.job.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.job.task.support.alarm.event.JobTaskFailAlarmEvent;
import com.old.silence.job.server.job.task.support.handler.JobTaskBatchHandler;
import com.old.silence.job.server.job.task.support.handler.WorkflowBatchHandler;
import com.old.silence.job.server.job.task.support.timer.JobTimerTask;
import com.old.silence.job.server.job.task.support.timer.JobTimerWheel;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;


@Component
public class JobTaskBatchGenerator {

    private final JobTaskBatchDao jobTaskBatchDao;
    private final WorkflowBatchHandler workflowBatchHandler;
    private final JobTaskBatchHandler jobTaskBatchHandler;
    private final JobDao jobDao;

    public JobTaskBatchGenerator(JobTaskBatchDao jobTaskBatchDao, WorkflowBatchHandler workflowBatchHandler,
                                 JobTaskBatchHandler jobTaskBatchHandler, JobDao jobDao) {
        this.jobTaskBatchDao = jobTaskBatchDao;
        this.workflowBatchHandler = workflowBatchHandler;
        this.jobTaskBatchHandler = jobTaskBatchHandler;
        this.jobDao = jobDao;
    }

    @Transactional
    public JobTaskBatch generateJobTaskBatch(JobTaskBatchGeneratorContext context) {

        // 生成一个新的任务
        JobTaskBatch jobTaskBatch = JobTaskConverter.INSTANCE.toJobTaskBatch(context);
        JobTaskExecutorScene jobTaskExecutorScene = context.getTaskExecutorScene();
        jobTaskBatch.setSystemTaskType(jobTaskExecutorScene.getSystemTaskType());
        jobTaskBatch.setCreatedDate(Instant.now());

        // 无执行的节点
        if (Objects.isNull(context.getOperationReason()) && Objects.isNull(context.getTaskBatchStatus()) &&
                CollectionUtils.isEmpty(CacheRegisterTable.getServerNodeSet(context.getGroupName(), context.getNamespaceId()))) {
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatus.CANCEL);
            jobTaskBatch.setOperationReason(JobOperationReason.NOT_CLIENT);
        } else {
            // 生成一个新的任务
            jobTaskBatch.setTaskBatchStatus(Optional.ofNullable(context.getTaskBatchStatus()).orElse(JobTaskBatchStatus.WAITING));
            jobTaskBatch.setOperationReason(context.getOperationReason());
        }

        try {
            Assert.isTrue(1 == jobTaskBatchDao.insert(jobTaskBatch), () -> new SilenceJobServerException("新增调度任务失败.jobId:[{}]", context.getJobId()));
        } catch (DuplicateKeyException ignored) {
            // 忽略重复的DAG任务
            return jobTaskBatchDao.selectOne(new LambdaQueryWrapper<JobTaskBatch>()
                    .eq(JobTaskBatch::getWorkflowTaskBatchId, context.getWorkflowTaskBatchId())
                    .eq(JobTaskBatch::getWorkflowNodeId, context.getWorkflowNodeId())
            );
        }

        // 无客户端节点-告警通知
        if (JobTaskBatchStatus.CANCEL.equals(jobTaskBatch.getTaskBatchStatus())
                && JobOperationReason.NOT_CLIENT.equals(jobTaskBatch.getOperationReason())) {
            var jobTaskFailAlarmEventDTO = new JobTaskFailAlarmEventDTO();
            jobTaskFailAlarmEventDTO.setJobTaskBatchId(jobTaskBatch.getId());
            jobTaskFailAlarmEventDTO.setNotifyScene(JobNotifyScene.JOB_NO_CLIENT_NODES_ERROR);
            jobTaskFailAlarmEventDTO.setReason(JobNotifyScene.JOB_NO_CLIENT_NODES_ERROR.getDescription());
            SilenceSpringContext.getContext().publishEvent(
                    new JobTaskFailAlarmEvent(jobTaskFailAlarmEventDTO));
            return jobTaskBatch;
        }

        // 非待处理状态无需进入时间轮中
        if (!JobTaskBatchStatus.WAITING.equals(jobTaskBatch.getTaskBatchStatus())) {

            // 开启下一个工作流
            openNextWorkflow(context, jobTaskBatch);

            // 若是常驻任务则需要再次进入时间轮
            openNextResidentTask(context, jobTaskBatch);
            return jobTaskBatch;
        }

        // 进入时间轮
        long delay = context.getNextTriggerAt() - DateUtils.toNowMilli();
        JobTimerTaskDTO jobTimerTaskDTO = new JobTimerTaskDTO();
        jobTimerTaskDTO.setTaskBatchId(jobTaskBatch.getId());
        jobTimerTaskDTO.setJobId(context.getJobId());
        jobTimerTaskDTO.setTaskExecutorScene(context.getTaskExecutorScene());
        jobTimerTaskDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
        jobTimerTaskDTO.setWorkflowNodeId(context.getWorkflowNodeId());
        jobTimerTaskDTO.setTmpArgsStr(context.getTmpArgsStr());
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    JobTimerWheel.registerWithJob(() -> new JobTimerTask(jobTimerTaskDTO), Duration.ofMillis(delay));
                }
            });
        } else {
            JobTimerWheel.registerWithJob(() -> new JobTimerTask(jobTimerTaskDTO), Duration.ofMillis(delay));
        }
        return jobTaskBatch;
    }

    private void openNextResidentTask(JobTaskBatchGeneratorContext context, JobTaskBatch jobTaskBatch) {

        // 手动触发的定时任务、工作流场景下不存在常驻任务，无需触发
        if (JobTaskExecutorScene.MANUAL_JOB.equals(context.getTaskExecutorScene())
                || JobTaskExecutorScene.AUTO_WORKFLOW.equals(context.getTaskExecutorScene())
                || JobTaskExecutorScene.MANUAL_WORKFLOW.equals(context.getTaskExecutorScene())) {
            return;
        }

        TaskExecuteDTO taskExecuteDTO = new TaskExecuteDTO();
        taskExecuteDTO.setTaskBatchId(jobTaskBatch.getId());
        taskExecuteDTO.setJobId(context.getJobId());
        taskExecuteDTO.setTaskExecutorScene(context.getTaskExecutorScene());
        taskExecuteDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
        taskExecuteDTO.setWorkflowNodeId(context.getWorkflowNodeId());
        Job job = jobDao.selectById(context.getJobId());
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    jobTaskBatchHandler.openResidentTask(job, taskExecuteDTO);
                }
            });
        } else {
            jobTaskBatchHandler.openResidentTask(job, taskExecuteDTO);
        }

    }

    private void openNextWorkflow(JobTaskBatchGeneratorContext context, JobTaskBatch jobTaskBatch) {
        WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
        taskExecuteDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
        taskExecuteDTO.setTaskExecutorScene(context.getTaskExecutorScene());
        taskExecuteDTO.setParentId(context.getWorkflowNodeId());
        taskExecuteDTO.setTaskBatchId(jobTaskBatch.getId());
        workflowBatchHandler.openNextNode(taskExecuteDTO);
    }

}
