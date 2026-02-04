package com.old.silence.job.server.retry.task.support;

import com.old.silence.job.server.retry.task.support.result.RetryResultContext;

/**
 * <p>
 *
 * </p>
 *
 */
public interface RetryResultHandler {

    boolean supports(RetryResultContext context);

    void handle(RetryResultContext context);
}
