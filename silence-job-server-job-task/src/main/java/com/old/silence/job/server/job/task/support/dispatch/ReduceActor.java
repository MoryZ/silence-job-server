package com.old.silence.job.server.job.task.support.dispatch;

import cn.hutool.core.lang.Assert;
import org.apache.pekko.actor.AbstractActor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.domain.model.WorkflowTaskBatch;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowTaskBatchDao;
import com.old.silence.job.server.job.task.dto.ReduceTaskDTO;
import com.old.silence.job.server.job.task.support.JobExecutor;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.job.task.support.executor.job.JobExecutorContext;
import com.old.silence.job.server.job.task.support.executor.job.JobExecutorFactory;
import com.old.silence.job.server.job.task.support.generator.task.JobTaskGenerateContext;
import com.old.silence.job.server.job.task.support.generator.task.JobTaskGenerator;
import com.old.silence.job.server.job.task.support.generator.task.JobTaskGeneratorFactory;
import com.old.silence.job.server.job.task.support.handler.DistributedLockHandler;
import com.old.silence.job.server.job.task.support.handler.JobTaskBatchHandler;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * 负责生成reduce任务并执行
 */
@Component(ActorGenerator.JOB_REDUCE_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReduceActor extends AbstractActor {

    private static final String KEY = "job_generate_reduce_{0}_{1}";
    private final DistributedLockHandler distributedLockHandler;
    private final JobDao jobDao;
    private final JobTaskDao jobTaskDao;
    private final WorkflowTaskBatchDao workflowTaskBatchDao;
    private final JobTaskBatchHandler jobTaskBatchHandler;

    public ReduceActor(DistributedLockHandler distributedLockHandler, JobDao jobDao,
                       JobTaskDao jobTaskDao, WorkflowTaskBatchDao workflowTaskBatchDao,
                       JobTaskBatchHandler jobTaskBatchHandler) {
        this.distributedLockHandler = distributedLockHandler;
        this.jobDao = jobDao;
        this.jobTaskDao = jobTaskDao;
        this.workflowTaskBatchDao = workflowTaskBatchDao;
        this.jobTaskBatchHandler = jobTaskBatchHandler;
    }

    private static JobExecutorContext buildJobExecutorContext(
            ReduceTaskDTO reduceTask,
            Job job,
            List<JobTask> taskList,
            String wfContext) {
        JobExecutorContext context = JobTaskConverter.INSTANCE.toJobExecutorContext(job);
        context.setTaskList(taskList);
        context.setTaskBatchId(reduceTask.getTaskBatchId());
        context.setWorkflowTaskBatchId(reduceTask.getWorkflowTaskBatchId());
        context.setWorkflowNodeId(reduceTask.getWorkflowNodeId());
        context.setWfContext(wfContext);
        return context;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ReduceTaskDTO.class, reduceTask -> {
            SilenceJobLog.LOCAL.info("执行Reduce, [{}]", JSON.toJSONString(reduceTask));
            try {

                Assert.notNull(reduceTask.getMrStage(), () -> new SilenceJobServerException("mrStage can not be null"));
                Assert.notNull(reduceTask.getJobId(), () -> new SilenceJobServerException("jobId can not be null"));
                Assert.notNull(reduceTask.getTaskBatchId(),
                        () -> new SilenceJobServerException("taskBatchId can not be null"));
                String key = MessageFormat.format(KEY, reduceTask.getTaskBatchId(), reduceTask.getJobId());
                distributedLockHandler.lockWithDisposableAndRetry(() -> {
                    doReduce(reduceTask);
                }, key, Duration.ofSeconds(1), Duration.ofSeconds(2), 6);
            } catch (Exception e) {
                SilenceJobLog.LOCAL.error("Reduce processing exception. [{}]", reduceTask, e);
            }

        }).build();
    }

    private void doReduce(ReduceTaskDTO reduceTask) {

        List<JobTask> jobTasks = jobTaskDao.selectList(
                new LambdaQueryWrapper<JobTask>()
                        .select(JobTask::getId)
                        .eq(JobTask::getTaskBatchId, reduceTask.getTaskBatchId())
                        .eq(JobTask::getMrStage, reduceTask.getMrStage())
                        .orderByAsc(JobTask::getId)
        );

        if (CollectionUtils.isNotEmpty(jobTasks)) {
            // 说明已经创建了reduce 或者 merge reduce 任务了
            return;
        }

        Job job = jobDao.selectById(reduceTask.getJobId());
        // 非MAP_REDUCE任务不处理
        if (!JobTaskType.MAP_REDUCE.equals(job.getTaskType())) {

            return;
        }

        String argStr = jobTaskBatchHandler.getArgStr(reduceTask.getTaskBatchId(), job);

        // 创建reduce任务
        JobTaskGenerator taskInstance = JobTaskGeneratorFactory.getTaskInstance(JobTaskType.MAP_REDUCE);
        JobTaskGenerateContext context = JobTaskConverter.INSTANCE.toJobTaskInstanceGenerateContext(job);
        context.setTaskBatchId(reduceTask.getTaskBatchId());
        context.setMrStage(reduceTask.getMrStage());
        context.setWfContext(reduceTask.getWfContext());
        context.setArgsStr(argStr);
        List<JobTask> taskList = taskInstance.generate(context);
        if (CollectionUtils.isEmpty(taskList)) {
            SilenceJobLog.LOCAL.warn("Job task is empty, taskBatchId:[{}]", reduceTask.getTaskBatchId());
            return;
        }

        String wfContext = null;
        if (Objects.nonNull(reduceTask.getWorkflowTaskBatchId())) {
            WorkflowTaskBatch workflowTaskBatch = workflowTaskBatchDao.selectOne(
                    new LambdaQueryWrapper<WorkflowTaskBatch>()
                            .select(WorkflowTaskBatch::getWfContext, WorkflowTaskBatch::getId)
                            .eq(WorkflowTaskBatch::getId, reduceTask.getWorkflowTaskBatchId())
            );
            wfContext = workflowTaskBatch.getWfContext();
        }

        // 执行任务
        JobExecutor jobExecutor = JobExecutorFactory.getJobExecutor(JobTaskType.MAP_REDUCE);
        jobExecutor.execute(buildJobExecutorContext(reduceTask, job, taskList, wfContext));

    }
}
