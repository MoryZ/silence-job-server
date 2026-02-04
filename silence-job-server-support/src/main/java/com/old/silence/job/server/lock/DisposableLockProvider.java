package com.old.silence.job.server.common.lock;

import com.old.silence.job.server.common.dto.LockConfig;
import com.old.silence.job.server.common.lock.persistence.LockStorage;
import com.old.silence.job.server.common.lock.persistence.LockStorageFactory;


public class DisposableLockProvider extends AbstractLockProvider {

    @Override
    protected boolean doLockAfter(LockConfig lockConfig) {
        return Boolean.FALSE;
    }

    @Override
    protected void doUnlock(LockConfig lockConfig) {
        doUnlockWithDelete(lockConfig);
    }

    protected boolean doUnlockWithDelete(LockConfig lockConfig) {
        LockStorage lockStorage = LockStorageFactory.getLockStorage();
        return lockStorage.releaseLockWithDelete(lockConfig.getLockName());
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
