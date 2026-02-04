package com.old.silence.job.server.retry.task.support.result;

import com.old.silence.job.server.retry.task.support.RetryResultHandler;

/**
 * <p>
 *
 * </p>
 *
 */
public abstract class AbstractRetryResultHandler implements RetryResultHandler {

    @Override
    public void handle(RetryResultContext context) {
        doHandler(context);
    }


    protected abstract void doHandler(RetryResultContext context);
}
