package com.old.silence.job.server.job.task.support.stop;

import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;


@Component
public class MapTaskStopHandler extends MapReduceTaskStopHandler {

    protected MapTaskStopHandler(JobTaskDao jobTaskDao, JobTaskBatchDao jobTaskBatchDao) {
        super(jobTaskDao, jobTaskBatchDao);
    }

    @Override
    public JobTaskType getTaskType() {
        return JobTaskType.MAP;
    }

    @Override
    protected void doStop(TaskStopJobContext context) {
        super.doStop(context);
    }
}
