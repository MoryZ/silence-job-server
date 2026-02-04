package com.old.silence.job.server.common.dto;

import java.time.Duration;
import java.time.Instant;

/**
 * 分布式锁配置
 *
 */
public class LockConfig {

    private Instant createdDate;

    private String lockName;

    private Duration lockAtMost;

    private Duration lockAtLeast;

    public Instant getcreatedDate() {
        return createdDate;
    }

    public String getLockName() {
        return lockName;
    }

    public void setcreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public void setLockAtMost(Duration lockAtMost) {
        this.lockAtMost = lockAtMost;
    }

    public void setLockAtLeast(Duration lockAtLeast) {
        this.lockAtLeast = lockAtLeast;
    }

    public Instant getLockAtMost() {
        return createdDate.plus(lockAtMost);
    }

    public Instant getLockAtLeast() {
        return createdDate.plus(lockAtLeast);
    }
}
