package com.old.silence.job.server.common.lock;


import java.time.Duration;


public interface LockProvider {

    boolean lock(Duration lockAtLeast, Duration lockAtMost);

    boolean lock(Duration lockAtMost);

    void unlock();

}
