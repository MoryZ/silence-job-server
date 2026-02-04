package com.old.silence.job.server.job.task.support;

import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.job.task.support.result.job.JobExecutorResultContext;

public interface JobExecutorResultHandler {

    JobTaskType getTaskInstanceType();

    void handleResult(JobExecutorResultContext context);
}
