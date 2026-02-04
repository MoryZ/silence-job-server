package com.old.silence.job.server.common.lock;

import cn.hutool.core.lang.Assert;

import com.old.silence.core.context.CommonErrors;
import com.old.silence.job.server.common.cache.CacheLockRecord;
import com.old.silence.job.server.common.dto.LockConfig;

import java.time.Duration;
import java.time.Instant;


public abstract class AbstractLockProvider implements LockProvider {

    @Override
    public boolean lock(Duration lockAtMost) {
        return lock(lockAtMost, lockAtMost);
    }

    @Override
    public boolean lock(Duration lockAtLeast, Duration lockAtMost) {
        LockConfig lockConfig = LockManager.getLockConfig();
        String lockName = lockConfig.getLockName();

        Assert.notNull(lockAtMost,
                () -> CommonErrors.INVALID_PARAMETER.createException("lockAtMost can not be null. lockName:[{}]", lockName));
        Assert.isFalse(lockAtMost.isNegative(),
                () -> CommonErrors.INVALID_PARAMETER.createException("lockAtMost  is negative. lockName:[{}]", lockName));
        Assert.notNull(lockAtLeast,
                () -> CommonErrors.INVALID_PARAMETER.createException("lockAtLeast can not be null. lockName:[{}]", lockName));
        Assert.isFalse(lockAtLeast.compareTo(lockAtMost) > 0,
                () -> CommonErrors.INVALID_PARAMETER.createException("lockAtLeast is longer than lockAtMost for lock. lockName:[{}]",
                        lockName));

        LockManager.setcreatedDate(Instant.now());
        LockManager.setLockAtLeast(lockAtLeast);
        LockManager.setLockAtMost(lockAtMost);

        boolean tryToCreateLockRecord = !CacheLockRecord.lockRecordRecentlyCreated(lockName);
        if (tryToCreateLockRecord) {
            if (doLock(lockConfig)) {
                CacheLockRecord.addLockRecord(lockName);
                return true;
            }
        }

        return doLockAfter(lockConfig);
    }

    protected abstract boolean doLockAfter(LockConfig lockConfig);

    protected boolean doLock(LockConfig lockConfig) {
        return createLock(lockConfig);
    }

    @Override
    public void unlock() {
        try {
            LockConfig lockConfig = LockManager.getLockConfig();
            Assert.notNull(lockConfig, () -> CommonErrors.INVALID_PARAMETER.createException("lockConfig can not be null."));
            doUnlock(lockConfig);
        } finally {
            LockManager.clear();
        }

    }

    protected abstract void doUnlock(LockConfig lockConfig);

    protected abstract boolean createLock(LockConfig lockConfig);

    protected abstract boolean renewal(LockConfig lockConfig);
}
