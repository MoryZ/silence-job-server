package com.old.silence.job.server.common.cache;

import org.apache.pekko.actor.ActorRef;
import org.springframework.stereotype.Component;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;

/**
 * 缓存组扫描Actor
 *
 */
@Component


public class CacheGroupScanActor implements Lifecycle {

    private static Cache<String, ActorRef> CACHE;

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static ActorRef get(String groupName, SystemTaskType typeEnum) {
        return CACHE.getIfPresent(groupName.concat(typeEnum.name()));
    }

    /**
     * 获取所有缓存
     *
     */
    public static void put(String groupName, SystemTaskType typeEnum, ActorRef actorRef) {
        CACHE.put(groupName.concat(typeEnum.name()), actorRef);
    }

    @Override
    public void start() {
        SilenceJobLog.LOCAL.info("CacheGroupScanActor start");
        CACHE = CacheBuilder.newBuilder()
                // 设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }

    @Override
    public void close() {
        SilenceJobLog.LOCAL.info("CacheGroupScanActor stop");
        CACHE.invalidateAll();
    }
}
