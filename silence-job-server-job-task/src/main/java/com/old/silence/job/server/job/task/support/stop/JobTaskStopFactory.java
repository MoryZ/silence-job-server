package com.old.silence.job.server.job.task.support.stop;

import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.job.task.support.JobTaskStopHandler;

import java.util.concurrent.ConcurrentHashMap;


public final class JobTaskStopFactory {

    private static final ConcurrentHashMap<JobTaskType, JobTaskStopHandler> CACHE = new ConcurrentHashMap<>();

    private JobTaskStopFactory() {
    }

    public static void registerTaskStop(JobTaskType taskInstanceType, JobTaskStopHandler interrupt) {
        CACHE.put(taskInstanceType, interrupt);
    }

    public static JobTaskStopHandler getJobTaskStop(JobTaskType jobTaskType) {
        return CACHE.get(jobTaskType);
    }

    public static JobTaskStopFactory createJobTaskStopFactory() {
        return new JobTaskStopFactory();
    }
}
