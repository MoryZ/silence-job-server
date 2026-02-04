package com.old.silence.job.server.common.lock;

import cn.hutool.core.lang.Assert;
import com.old.silence.job.server.exception.SilenceJobServerException;


public final class LockBuilder {


    private String lockName;

    private boolean resident;

    public static LockBuilder newBuilder() {
        return new LockBuilder();
    }

    public LockBuilder withResident(String lockName) {
        this.lockName = lockName;
        resident = Boolean.TRUE;
        return this;
    }

    public LockBuilder withDisposable(String lockName) {
        this.lockName = lockName;
        resident = Boolean.FALSE;
        return this;
    }

    public LockProvider build() {
        Assert.notBlank(lockName, () -> new SilenceJobServerException("lockName can not be null."));

        LockManager.initialize();
        LockManager.setLockName(lockName);
        if (resident) {
            return new ResidentLockProvider();
        } else {
            return new DisposableLockProvider();
        }
    }

}
