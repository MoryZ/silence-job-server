package com.old.silence.job.server.common.lock.persistence;


import java.util.ArrayList;
import java.util.List;

import com.old.silence.core.context.CommonErrors;


public final class LockStorageFactory {

    private static final List<LockStorage> LOCK_STORAGES = new ArrayList<>();

    public static void registerLockStorage(LockStorage lockStorage) {
        LOCK_STORAGES.add(lockStorage);
    }

    public static LockStorage getLockStorage() {
        return LOCK_STORAGES.stream()
                .findFirst().orElseThrow(() -> CommonErrors.FATAL_ERROR.createException("未找到合适锁处理器"));
    }

}
