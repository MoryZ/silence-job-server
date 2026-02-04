package com.old.silence.job.server.job.task.support.prepare.job;

import com.old.silence.job.server.job.task.dto.JobTaskPrepareDTO;
import com.old.silence.job.server.job.task.support.JobPrepareHandler;


public abstract class AbstractJobPrepareHandler implements JobPrepareHandler {

    @Override
    public void handle(JobTaskPrepareDTO jobPrepareDTO) {
        doHandle(jobPrepareDTO);
    }

    protected abstract void doHandle(JobTaskPrepareDTO jobPrepareDTO);
}
