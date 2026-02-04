package com.old.silence.job.server.common.lock;

import com.old.silence.job.server.common.cache.CacheLockRecord;
import com.old.silence.job.server.common.dto.LockConfig;
import com.old.silence.job.server.common.lock.persistence.LockStorage;
import com.old.silence.job.server.common.lock.persistence.LockStorageFactory;


public class ResidentLockProvider extends AbstractLockProvider {

    @Override
    protected boolean doLockAfter(LockConfig lockConfig) {
        String lockName = lockConfig.getLockName();
        boolean lock;
        try {
            lock = renewal(lockConfig);
            if (lock) {
                CacheLockRecord.addLockRecord(lockName);
            }
        } catch (Exception e) {
            CacheLockRecord.remove(lockName);
            throw e;
        }

        return lock;
    }

    @Override
    protected void doUnlock(LockConfig lockConfig) {
        doUnlockWithUpdate(lockConfig);
    }

    protected void doUnlockWithUpdate(LockConfig lockConfig) {
        LockStorage lockStorage = LockStorageFactory.getLockStorage();
        lockStorage.releaseLockWithUpdate(lockConfig.getLockName(), lockConfig.getLockAtLeast());
    }

    @Override
    protected boolean createLock(LockConfig lockConfig) {
        LockStorage lockStorage = LockStorageFactory.getLockStorage();
        return lockStorage.createLock(lockConfig);
    }

    @Override
    protected boolean renewal(LockConfig lockConfig) {
        LockStorage lockStorage = LockStorageFactory.getLockStorage();
        return lockStorage.renewal(lockConfig);
    }
}
