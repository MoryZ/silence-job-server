package com.old.silence.job.server.job.task.support;

import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.job.task.support.callback.ClientCallbackContext;


public interface ClientCallbackHandler {

    JobTaskType getTaskInstanceType();

    void callback(ClientCallbackContext context);
}
