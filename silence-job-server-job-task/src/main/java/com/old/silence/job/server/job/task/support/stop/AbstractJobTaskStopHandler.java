package com.old.silence.job.server.job.task.support.stop;

import org.apache.pekko.actor.ActorRef;
import org.springframework.beans.factory.InitializingBean;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.common.enums.JobTaskStatus;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.job.task.dto.JobExecutorResultDTO;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.job.task.support.JobTaskStopHandler;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.util.List;

public abstract class AbstractJobTaskStopHandler implements JobTaskStopHandler, InitializingBean {

    private final JobTaskDao jobTaskDao;
    private final JobTaskBatchDao jobTaskBatchDao;

    protected AbstractJobTaskStopHandler(JobTaskDao jobTaskDao, JobTaskBatchDao jobTaskBatchDao) {
        this.jobTaskDao = jobTaskDao;
        this.jobTaskBatchDao = jobTaskBatchDao;
    }

    protected abstract void doStop(TaskStopJobContext context);

    @Override
    public void stop(TaskStopJobContext context) {

        LambdaQueryWrapper<JobTask> queryWrapper = new LambdaQueryWrapper<JobTask>()
                .eq(JobTask::getTaskBatchId, context.getTaskBatchId());

        if (!context.isForceStop()) {
            queryWrapper.in(JobTask::getTaskStatus, JobTaskStatus.NOT_COMPLETE);
        }

        List<JobTask> jobTasks = jobTaskDao.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(jobTasks)) {
            // 若没有任务项，直接变更状态为已停止
            JobTaskBatch jobTaskBatch = new JobTaskBatch();
            jobTaskBatch.setId(context.getTaskBatchId());
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatus.STOP);
            jobTaskBatch.setOperationReason(context.getJobOperationReason());
            jobTaskBatchDao.updateById(jobTaskBatch);
            return;
        }

        context.setJobTasks(jobTasks);

        doStop(context);

        if (context.isNeedUpdateTaskStatus()) {
            for (JobTask jobTask : jobTasks) {
                if (jobTask.getTaskStatus() == JobTaskStatus.SUCCESS) {
                    continue;
                }
                JobExecutorResultDTO jobExecutorResultDTO = JobTaskConverter.INSTANCE.toJobExecutorResultDTO(jobTask);
                jobExecutorResultDTO.setTaskStatus(JobTaskStatus.STOP);
                jobExecutorResultDTO.setMessage("任务停止成功");
                jobExecutorResultDTO.setJobOperationReason(context.getJobOperationReason());
                jobExecutorResultDTO.setTaskType(getTaskType());
                jobExecutorResultDTO.setWorkflowNodeId(context.getWorkflowNodeId());
                jobExecutorResultDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
                ActorRef actorRef = ActorGenerator.jobTaskExecutorResultActor();
                actorRef.tell(jobExecutorResultDTO, actorRef);
            }

        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        JobTaskStopFactory.registerTaskStop(getTaskType(), this);
    }
}
