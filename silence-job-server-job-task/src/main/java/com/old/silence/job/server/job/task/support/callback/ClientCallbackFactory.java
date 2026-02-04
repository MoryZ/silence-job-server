package com.old.silence.job.server.job.task.support.callback;

import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.job.task.support.ClientCallbackHandler;

import java.util.concurrent.ConcurrentHashMap;


public class ClientCallbackFactory {

    private static final ConcurrentHashMap<JobTaskType, ClientCallbackHandler> CACHE = new ConcurrentHashMap<>();

    public static void registerJobExecutor(JobTaskType taskInstanceType, ClientCallbackHandler callbackHandler) {
        CACHE.put(taskInstanceType, callbackHandler);
    }

    public static ClientCallbackHandler getClientCallback(JobTaskType jobTaskType) {
        return CACHE.get(jobTaskType);
    }
}
