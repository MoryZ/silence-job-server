package com.old.silence.job.server.common.lock;

import com.old.silence.job.server.common.dto.LockConfig;

import java.time.Duration;
import java.time.Instant;


public final class LockManager {
    private static final ThreadLocal<LockConfig> LOCK_CONFIG = new ThreadLocal<>();

    public static LockConfig getLockConfig() {
        return LOCK_CONFIG.get();
    }

    public static void initialize() {
        LOCK_CONFIG.set(new LockConfig());
    }

    public static void clear() {
        LOCK_CONFIG.remove();
    }

    public static void setLockName(String lockName) {
        getLockConfig().setLockName(lockName);
    }

    public static void setLockAtLeast(Duration lockAtLeast) {

        getLockConfig().setLockAtLeast(lockAtLeast);
    }

    public static void setcreatedDate(Instant createdDate) {
        getLockConfig().setcreatedDate(createdDate);
    }

    public static void setLockAtMost(Duration lockAtMost) {
        getLockConfig().setLockAtMost(lockAtMost);
    }


}
