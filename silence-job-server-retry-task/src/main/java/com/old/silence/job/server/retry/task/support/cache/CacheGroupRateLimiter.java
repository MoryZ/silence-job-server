package com.old.silence.job.server.retry.task.support.cache;

import org.springframework.stereotype.Component;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;

/**
 * 缓存组组限流组件
 *
 */
@Component


public class CacheGroupRateLimiter implements Lifecycle {

    private static Cache<String, RateLimiter> CACHE;

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static Cache<String, RateLimiter> getAll() {
        return CACHE;
    }

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static RateLimiter getRateLimiterByKey(String hostId) {
        return CACHE.getIfPresent(hostId);
    }

    @Override
    public void start() {
        SilenceJobLog.LOCAL.info("CacheGroupRateLimiter start");
        CACHE = CacheBuilder.newBuilder()
                // 设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }

    @Override
    public void close() {
        SilenceJobLog.LOCAL.info("CacheGroupRateLimiter stop");
    }
}
