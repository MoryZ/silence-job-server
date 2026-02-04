package com.old.silence.job.server.job.task.support.callback;

import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowTaskBatchDao;


@Component
public class MapClientCallbackHandler extends MapReduceClientCallbackHandler {


    public MapClientCallbackHandler(JobTaskDao jobTaskDao, JobDao jobDao, WorkflowTaskBatchDao workflowTaskBatchDao) {
        super(jobTaskDao, jobDao, workflowTaskBatchDao);
    }

    @Override
    public JobTaskType getTaskInstanceType() {
        return JobTaskType.MAP;
    }

    @Override
    protected void doCallback(ClientCallbackContext context) {
        super.doCallback(context);
    }
}
