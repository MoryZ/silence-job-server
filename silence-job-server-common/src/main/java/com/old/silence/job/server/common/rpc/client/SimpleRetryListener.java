package com.old.silence.job.server.common.rpc.client;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryListener;


public class SimpleRetryListener implements RetryListener {

    @Override
    public <V> void onRetry(Attempt<V> attempt) {

    }
}
