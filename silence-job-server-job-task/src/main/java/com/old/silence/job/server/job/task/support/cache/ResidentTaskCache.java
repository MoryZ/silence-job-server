package com.old.silence.job.server.job.task.support.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


public class ResidentTaskCache {

    private static final Cache<BigInteger, Long/*ms*/> cache;

    static {
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel(8) // 并发级别
                .expireAfterWrite(10, TimeUnit.SECONDS) // 写入后的过期时间
                .build();
    }

    public static void refresh(BigInteger jobId, Long nextTriggerTime) {
        cache.put(jobId, nextTriggerTime);
    }

    public static Long getOrDefault(BigInteger jobId, Long nextTriggerTime) {
        return Optional.ofNullable(cache.getIfPresent(jobId)).orElse(nextTriggerTime);
    }

    public static Long get(BigInteger jobId) {
        return getOrDefault(jobId, null);
    }

    public static boolean isResident(BigInteger jobId) {
        return cache.asMap().containsKey(jobId);
    }

}
