package com.old.silence.job.server.job.task.support.result.job;

import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.infrastructure.persistence.dao.GroupConfigDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.job.task.support.handler.WorkflowBatchHandler;

@Component
public class ShardingJobExecutorHandler extends AbstractJobExecutorResultHandler {

    public ShardingJobExecutorHandler(
            JobTaskDao jobTaskDao,
            JobTaskBatchDao jobTaskBatchDao,
            WorkflowBatchHandler workflowBatchHandler,
            GroupConfigDao groupConfigDao) {
        super(jobTaskDao, jobTaskBatchDao, workflowBatchHandler, groupConfigDao);
    }

    @Override
    public JobTaskType getTaskInstanceType() {
        return JobTaskType.SHARDING;
    }

    @Override
    protected void doHandleSuccess(JobExecutorResultContext context) {
    }

    @Override
    protected void doHandleStop(JobExecutorResultContext context) {

    }

    @Override
    protected void doHandleFail(JobExecutorResultContext context) {

    }

}
