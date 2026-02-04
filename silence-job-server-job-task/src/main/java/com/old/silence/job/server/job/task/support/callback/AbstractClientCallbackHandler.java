package com.old.silence.job.server.job.task.support.callback;

import cn.hutool.core.util.StrUtil;

import org.apache.pekko.actor.ActorRef;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.old.silence.job.common.enums.JobTaskStatus;
import com.old.silence.job.server.common.pekko.ActorGenerator;
import com.old.silence.job.server.common.util.ClientInfoUtils;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.domain.model.WorkflowTaskBatch;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowTaskBatchDao;
import com.old.silence.job.server.job.task.dto.RealJobExecutorDTO;
import com.old.silence.job.server.job.task.enums.JobRetrySceneEnum;
import com.old.silence.job.server.job.task.support.ClientCallbackHandler;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.job.task.support.timer.JobTimerWheel;
import com.old.silence.job.server.job.task.support.timer.RetryJobTimerTask;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Objects;


public abstract class AbstractClientCallbackHandler implements ClientCallbackHandler, InitializingBean {

    protected final JobTaskDao jobTaskDao;
    private final JobDao jobDao;
    private final WorkflowTaskBatchDao workflowTaskBatchDao;

    public AbstractClientCallbackHandler(JobTaskDao jobTaskDao, JobDao jobDao, WorkflowTaskBatchDao workflowTaskBatchDao) {
        this.jobTaskDao = jobTaskDao;
        this.jobDao = jobDao;
        this.workflowTaskBatchDao = workflowTaskBatchDao;
    }

    @Override
    @Transactional
    public void callback(ClientCallbackContext context) {

        // 判定是否需要重试
        boolean needRetry = isNeedRetry(context);
        if (needRetry && updateRetryCount(context)) {
            Job job = context.getJob();
            JobTask jobTask = context.getJobTask();
            RealJobExecutorDTO realJobExecutor = JobTaskConverter.INSTANCE.toRealJobExecutorDTO(
                    JobTaskConverter.INSTANCE.toJobExecutorContext(job), jobTask);
            realJobExecutor.setClientId(ClientInfoUtils.clientId(context.getClientInfo()));
            realJobExecutor.setWorkflowNodeId(context.getWorkflowNodeId());
            realJobExecutor.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
            realJobExecutor.setRetryCount(jobTask.getRetryCount() + 1);
            realJobExecutor.setRetryStatus(Boolean.TRUE);
            realJobExecutor.setRetryScene(context.getRetryScene());
            realJobExecutor.setTaskName(jobTask.getTaskName());
            // 这里统一收口传递上下文
            if (StrUtil.isBlank(realJobExecutor.getWfContext())) {
                realJobExecutor.setWfContext(getWfContext(realJobExecutor.getWorkflowTaskBatchId()));
            }
            if (JobRetrySceneEnum.MANUAL.getRetryScene().equals(context.getRetryScene())) {
                // 手动重试, 则即时重试
                ActorRef actorRef = ActorGenerator.jobRealTaskExecutorActor();
                actorRef.tell(realJobExecutor, actorRef);
            } else {
                // 注册重试任务，重试间隔时间轮
                JobTimerWheel.registerWithJob(() -> new RetryJobTimerTask(realJobExecutor), Duration.ofSeconds(job.getRetryInterval()));
            }

            return;
        }

        // 不需要重试执行回调
        doCallback(context);
    }

    /**
     * 获取工作流批次
     *
     * @param workflowTaskBatchId 工作流批次
     * @return 工作流上下文
     */
    private String getWfContext(BigInteger workflowTaskBatchId) {
        if (Objects.isNull(workflowTaskBatchId)) {
            return null;
        }

        WorkflowTaskBatch workflowTaskBatch = workflowTaskBatchDao.selectOne(
                new LambdaQueryWrapper<WorkflowTaskBatch>()
                        .select(WorkflowTaskBatch::getWfContext)
                        .eq(WorkflowTaskBatch::getId, workflowTaskBatchId)
        );

        if (Objects.isNull(workflowTaskBatch)) {
            return null;
        }

        return workflowTaskBatch.getWfContext();
    }

    private boolean updateRetryCount(ClientCallbackContext context) {
        JobTask updateJobTask = new JobTask();
        updateJobTask.setRetryCount(1);
        String newClient = chooseNewClient(context);
        if (StrUtil.isNotBlank(newClient)) {
            updateJobTask.setClientInfo(newClient);
            // 覆盖老的的客户端信息
            context.setClientInfo(newClient);
        } else {
            context.setClientInfo(context.getJobTask().getClientInfo());
        }

        Job job = context.getJob();
        LambdaUpdateWrapper<JobTask> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(JobTask::getId, context.getTaskId());
        if (Objects.isNull(context.getRetryScene())
                || Objects.equals(JobRetrySceneEnum.AUTO.getRetryScene(), context.getRetryScene())) {
            updateWrapper.lt(JobTask::getRetryCount, job.getMaxRetryTimes());
        }

        return SqlHelper.retBool(jobTaskDao.update(updateJobTask, updateWrapper));

    }

    private boolean isNeedRetry(ClientCallbackContext context) {

        JobTask jobTask = jobTaskDao.selectById(context.getTaskId());
        Job job = jobDao.selectById(context.getJobId());
        context.setJob(job);
        context.setJobTask(jobTask);
        if (Objects.isNull(jobTask) || Objects.isNull(job)) {
            return Boolean.FALSE;
        }

        // 手动重试策略
        if (Objects.nonNull(context.getRetryScene())
                && Objects.equals(JobRetrySceneEnum.MANUAL.getRetryScene(), context.getRetryScene())
                && !context.getRetryStatus()) {
            return Boolean.TRUE;
        }

        if (context.getTaskStatus().equals(JobTaskStatus.FAIL)) {
            if (jobTask.getRetryCount() < job.getMaxRetryTimes()) {
                context.setRetryScene(JobRetrySceneEnum.AUTO.getRetryScene());
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    protected abstract String chooseNewClient(ClientCallbackContext context);

    protected abstract void doCallback(ClientCallbackContext context);

    @Override
    public void afterPropertiesSet() throws Exception {
        ClientCallbackFactory.registerJobExecutor(getTaskInstanceType(), this);
    }
}
