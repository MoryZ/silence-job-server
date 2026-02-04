package com.old.silence.job.server.task.common.idempotent;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.old.silence.job.server.common.IdempotentStrategy;

import java.util.concurrent.TimeUnit;

/**
 * 时间轮幂等性实现（泛型化，支持配置）
 * 统一 Job 和 Retry 模块的幂等性逻辑
 * 
 * @author mory
 */
public class TimerIdempotent implements IdempotentStrategy<String> {

    private final Cache<String, String> cache;

    /**
     * 构造函数，支持配置并发级别和过期时间
     *
     * @param concurrencyLevel 并发级别
     * @param expireAfterWrite 写入后过期时间
     * @param timeUnit 时间单位
     */
    public TimerIdempotent(int concurrencyLevel, long expireAfterWrite, TimeUnit timeUnit) {
        this.cache = CacheBuilder.newBuilder()
                .concurrencyLevel(concurrencyLevel)
                .expireAfterWrite(expireAfterWrite, timeUnit)
                .build();
    }

    /**
     * 默认构造函数
     * 默认并发级别 16，过期时间 20 秒
     */
    public TimerIdempotent() {
        this(16, 20, TimeUnit.SECONDS);
    }

    @Override
    public boolean set(String key) {
        cache.put(key, key);
        return Boolean.TRUE;
    }

    @Override
    public boolean isExist(String key) {
        return cache.asMap().containsKey(key);
    }

    @Override
    public boolean clear(String key) {
        cache.invalidate(key);
        return Boolean.TRUE;
    }
}
