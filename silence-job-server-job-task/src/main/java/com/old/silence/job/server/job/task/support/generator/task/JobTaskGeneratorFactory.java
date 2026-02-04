package com.old.silence.job.server.job.task.support.generator.task;

import com.old.silence.job.common.enums.JobTaskType;

import java.util.concurrent.ConcurrentHashMap;


public final class JobTaskGeneratorFactory {

    private static final ConcurrentHashMap<JobTaskType, JobTaskGenerator> CACHE = new ConcurrentHashMap<>();

    public static void registerTaskInstance(JobTaskType taskInstanceType, JobTaskGenerator generator) {
        CACHE.put(taskInstanceType, generator);
    }

    public static JobTaskGenerator getTaskInstance(JobTaskType jobTaskType) {
        return CACHE.get(jobTaskType);
    }
}
