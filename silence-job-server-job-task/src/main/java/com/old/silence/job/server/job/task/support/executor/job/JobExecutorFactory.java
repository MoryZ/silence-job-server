package com.old.silence.job.server.job.task.support.executor.job;

import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.job.task.support.JobExecutor;

import java.util.concurrent.ConcurrentHashMap;


public class JobExecutorFactory {

    private static final ConcurrentHashMap<JobTaskType, JobExecutor> CACHE = new ConcurrentHashMap<>();

    public static void registerJobExecutor(JobTaskType taskInstanceType, JobExecutor executor) {
        CACHE.put(taskInstanceType, executor);
    }

    public static JobExecutor getJobExecutor(JobTaskType jobTaskType) {
        return CACHE.get(jobTaskType);
    }
}
