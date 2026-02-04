package com.old.silence.job.server.common.cache;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;
import com.old.silence.job.server.common.lock.LockManager;

import java.time.Duration;

/**
 * 缓存本地的分布式锁的名称
 *
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CacheLockRecord implements Lifecycle {
    private static Cache<String, String> CACHE;

    public static void addLockRecord(String lockName) {
        CACHE.put(lockName, lockName);
    }

    public static boolean lockRecordRecentlyCreated(String lockName) {
        return CACHE.asMap().containsKey(lockName);
    }

    public static long getSize() {
        return CACHE.size();
    }

    public static void remove(String lockName) {
        CACHE.invalidate(lockName);
        LockManager.clear();
    }

    public static void clear() {
        CACHE.invalidateAll();
    }

    @Override
    public void start() {
        SilenceJobLog.LOCAL.info("CacheLockRecord start");
        CACHE = CacheBuilder.newBuilder()
                // 设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .expireAfterWrite(Duration.ofHours(1))
                .build();
    }

    @Override
    public void close() {
        CACHE.invalidateAll();
    }
}
