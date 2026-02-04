package com.old.silence.job.server.job.task.support.result.job;

import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.infrastructure.persistence.dao.GroupConfigDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.job.task.support.handler.WorkflowBatchHandler;


@Component
public class ClusterJobExecutorHandler extends AbstractJobExecutorResultHandler {

    public ClusterJobExecutorHandler(
            final JobTaskDao jobTaskDao,
            final JobTaskBatchDao jobTaskBatchDao,
            final WorkflowBatchHandler workflowBatchHandler,
            final GroupConfigDao groupConfigDao) {
        super(jobTaskDao, jobTaskBatchDao, workflowBatchHandler, groupConfigDao);
    }

    @Override
    public JobTaskType getTaskInstanceType() {
        return JobTaskType.CLUSTER;
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

    @Override
    protected void stop(JobExecutorResultContext context) {
    }
}
