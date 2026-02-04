package com.old.silence.job.server.common.cache;

import cn.hutool.core.util.StrUtil;

import org.springframework.stereotype.Component;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;
import com.old.silence.job.server.common.config.SystemProperties;
import com.old.silence.job.server.common.register.ServerRegister;
import com.old.silence.job.server.common.triple.Pair;
import com.old.silence.job.server.domain.model.GroupConfig;
import com.old.silence.job.server.domain.service.AccessTemplate;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Component
public class CacheToken implements Lifecycle {

    private static Cache<Pair<String/*groupName*/, String/*namespaceId*/>, String/*Token*/> CACHE;

    public static void add(String groupName, String namespaceId, String token) {
        CACHE.put(Pair.of(groupName, namespaceId), token);
    }

    public static String get(String groupName, String namespaceId) {
        if (groupName.equals(ServerRegister.GROUP_NAME)){
            return getServerToken();
        }
        String token = CACHE.getIfPresent(Pair.of(groupName, namespaceId));
        if (StrUtil.isBlank(token)) {
            // 从DB获取数据
            AccessTemplate template = SilenceSpringContext.getBean(AccessTemplate.class);
            GroupConfig config = template.getGroupConfigAccess().getGroupConfigByGroupName(groupName, namespaceId);
            if (Objects.isNull(config)) {
                return StrUtil.EMPTY;
            }

            token = config.getToken();
            add(groupName, namespaceId, token);
        }

        return token;
    }

    private static String getServerToken() {
        SystemProperties properties = SilenceSpringContext.getBean(SystemProperties.class);
        return properties.getServerToken();
    }

    @Override
    public void start() {
        SilenceJobLog.LOCAL.info("CacheToken start");
        CACHE = CacheBuilder.newBuilder()
                // 设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                // 若当前节点不在消费次组，则自动到期删除
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void close() {
        SilenceJobLog.LOCAL.info("CacheToken stop");
        CACHE.invalidateAll();
    }
}
