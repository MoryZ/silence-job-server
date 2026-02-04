package com.old.silence.job.server.job.task.support;

import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.job.task.support.executor.job.JobExecutorContext;


public interface JobExecutor {

    JobTaskType getTaskInstanceType();

    void execute(JobExecutorContext context);
}
