package com.old.silence.job.server.common;


public interface TimerTask<T> extends io.netty.util.TimerTask {

    T idempotentKey();
}
