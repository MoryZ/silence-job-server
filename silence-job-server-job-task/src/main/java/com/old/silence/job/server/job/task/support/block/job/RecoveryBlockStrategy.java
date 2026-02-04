package com.old.silence.job.server.job.task.support.block.job;

import cn.hutool.core.lang.Assert;
import org.apache.pekko.actor.ActorRef;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.common.enums.JobTaskExecutorScene;
import com.old.silence.job.common.enums.JobTaskStatus;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.job.task.dto.TaskExecuteDTO;
import com.old.silence.job.server.job.task.support.JobExecutor;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.job.task.support.executor.job.JobExecutorContext;
import com.old.silence.job.server.job.task.support.executor.job.JobExecutorFactory;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.util.List;

/**
 * 重新触发执行失败的任务
 */
@Component
public class RecoveryBlockStrategy extends AbstracJobBlockStrategy {

    private final JobTaskDao jobTaskDao;
    private final JobDao jobDao;

    public RecoveryBlockStrategy(JobTaskDao jobTaskDao, JobDao jobDao) {
        this.jobTaskDao = jobTaskDao;
        this.jobDao = jobDao;
    }

    private static JobExecutorContext buildJobExecutorContext(BlockStrategyContext strategyContext, Job job,
                                                              List<JobTask> taskList) {
        JobExecutorContext context = JobTaskConverter.INSTANCE.toJobExecutorContext(job);
        context.setTaskList(taskList);
        context.setTaskBatchId(strategyContext.getTaskBatchId());
        context.setWorkflowTaskBatchId(strategyContext.getWorkflowTaskBatchId());
        context.setWorkflowNodeId(strategyContext.getWorkflowNodeId());
        return context;
    }

    @Override
    protected void doBlock(BlockStrategyContext context) {
        Assert.notNull(context.getJobId(), () -> new SilenceJobServerException("job id can not be null"));
        Assert.notNull(context.getTaskBatchId(), () -> new SilenceJobServerException("task batch id can not be null"));
        Assert.notNull(context.getTaskType(), () -> new SilenceJobServerException("task type can not be null"));

        List<JobTask> jobTasks = jobTaskDao.selectList(
                new LambdaQueryWrapper<JobTask>()
                        .eq(JobTask::getTaskBatchId, context.getTaskBatchId())
        );

        //  若任务项为空则生成任务项
        if (CollectionUtils.isEmpty(jobTasks)) {
            TaskExecuteDTO taskExecuteDTO = new TaskExecuteDTO();
            taskExecuteDTO.setTaskBatchId(context.getTaskBatchId());
            taskExecuteDTO.setJobId(context.getJobId());
            taskExecuteDTO.setTaskExecutorScene(JobTaskExecutorScene.MANUAL_JOB);
            taskExecuteDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
            taskExecuteDTO.setWorkflowNodeId(context.getWorkflowNodeId());
            ActorRef actorRef = ActorGenerator.jobTaskExecutorActor();
            actorRef.tell(taskExecuteDTO, actorRef);
            return;
        }

        Job job = jobDao.selectById(context.getJobId());
        // 执行任务 Stop or Fail 任务
        JobExecutor jobExecutor = JobExecutorFactory.getJobExecutor(context.getTaskType());
        jobExecutor.execute(buildJobExecutorContext(context, job,
                StreamUtils.filter(jobTasks,
                        (jobTask) -> JobTaskStatus.NOT_SUCCESS.contains(jobTask.getTaskStatus())
                )));
    }

    @Override
    protected JobBlockStrategy blockStrategyEnum() {
        return JobBlockStrategy.RECOVERY;
    }

}
