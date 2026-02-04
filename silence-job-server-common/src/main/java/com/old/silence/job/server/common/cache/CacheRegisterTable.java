package com.old.silence.job.server.common.cache;

import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;
import com.old.silence.job.server.common.convert.RegisterNodeInfoConverter;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;
import com.old.silence.job.server.common.register.ServerRegister;
import com.old.silence.job.server.domain.model.ServerNode;
import com.old.silence.job.server.infrastructure.persistence.dao.ServerNodeDao;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * POD注册表
 *
 */
@Component
public class CacheRegisterTable implements Lifecycle {

    private static final Cache<String, ConcurrentMap<String, RegisterNodeInfo>> CACHE;

    static {
        CACHE = CacheBuilder.newBuilder()
                // 设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                // 设置写缓存后60秒过期
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static Set<RegisterNodeInfo> getAllPods() {
        ConcurrentMap<String, ConcurrentMap<String, RegisterNodeInfo>> concurrentMap = CACHE.asMap();
        if (CollectionUtils.isEmpty(concurrentMap)) {
            return Sets.newHashSet();
        }

        return concurrentMap.values().stream()
                .map(stringServerNodeConcurrentMap -> new TreeSet(stringServerNodeConcurrentMap.values()))
                .reduce((s, y) -> {
                    s.addAll(y);
                    return s;
                }).orElse(new TreeSet<>());

    }

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static ConcurrentMap<String, RegisterNodeInfo> get(String groupName) {
        return CACHE.getIfPresent(groupName);
    }

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static RegisterNodeInfo getServerNode(String groupName, String namespaceId, String hostId) {
        // TODO 这里namespaceId 也没有用到
        ConcurrentMap<String, RegisterNodeInfo> concurrentMap = CACHE.getIfPresent(groupName);
        if (Objects.isNull(concurrentMap)) {
            // 此处为了降级，若缓存中没有则取DB中查询
            ServerNodeDao serverNodeDao = SilenceSpringContext.getBeanByType(ServerNodeDao.class);
            List<ServerNode> serverNodes = serverNodeDao.selectList(
                    new LambdaQueryWrapper<ServerNode>()
                            .eq(ServerNode::getGroupName, groupName)
                            .eq(ServerNode::getHostId, hostId)
                            .orderByDesc(ServerNode::getExpireAt));
            if (CollectionUtils.isEmpty(serverNodes)) {
                return null;
            }

            CacheRegisterTable.addOrUpdate(serverNodes.getFirst());

            concurrentMap = CACHE.getIfPresent(groupName);
            if (CollectionUtils.isEmpty(concurrentMap)) {
                return null;
            }
        }

        return concurrentMap.get(hostId);
    }

    /**
     * 获取排序的ServerNode
     *
     * @return 缓存对象
     */
    public static Set<RegisterNodeInfo> getServerNodeSet(String groupName, String namespaceId) {
        //TODO 并没有使用到namespaceId 后续结合语境修改
        ConcurrentMap<String, RegisterNodeInfo> concurrentMap = CACHE.getIfPresent(groupName);
        if (CollectionUtils.isEmpty(concurrentMap)) {

            // 此处为了降级，若缓存中没有则取DB中查询
            ServerNodeDao serverNodeDao = SilenceSpringContext.getBeanByType(ServerNodeDao.class);
            List<ServerNode> serverNodes = serverNodeDao.selectList(
                    new LambdaQueryWrapper<ServerNode>()
                            .eq(ServerNode::getGroupName, groupName));
            for (ServerNode node : serverNodes) {
                // 刷新全量本地缓存
                CacheRegisterTable.addOrUpdate(node);
            }

            concurrentMap = CACHE.getIfPresent(groupName);
            if (CollectionUtils.isEmpty(serverNodes) || CollectionUtils.isEmpty(concurrentMap)) {
                return Sets.newHashSet();
            }
        }

        return new TreeSet<>(concurrentMap.values());
    }


    /**
     * 获取排序的hostId
     *
     * @return 缓存对象
     */
    public static Set<String> getPodIdSet(String groupName) {
        //TODO 并没有使用到namespaceId
        return StreamUtils.toSet(getServerNodeSet(groupName, null), RegisterNodeInfo::getHostId);
    }


    /**
     * 刷新过期时间若不存在则初始化
     *
     * @param serverNode 服务节点
     */
    public static synchronized void refreshExpireAt(ServerNode serverNode) {
        RegisterNodeInfo registerNodeInfo = getServerNode(serverNode.getGroupName(), serverNode.getNamespaceId(),
                serverNode.getHostId());
        // 不存在则初始化
        if (Objects.isNull(registerNodeInfo)) {
            SilenceJobLog.LOCAL.warn("node not exists. groupName:[{}] hostId:[{}]", serverNode.getGroupName(),
                    serverNode.getHostId());
        } else {
            // 存在则刷新过期时间
            registerNodeInfo.setExpireAt(serverNode.getExpireAt());
        }
    }

    /**
     * 无缓存时添加 有缓存时更新
     *
     */
    public static synchronized void addOrUpdate(ServerNode serverNode) {
        ConcurrentMap<String, RegisterNodeInfo> concurrentMap = CACHE.getIfPresent(serverNode.getGroupName());
        RegisterNodeInfo registerNodeInfo;
        if (Objects.isNull(concurrentMap)) {
            SilenceJobLog.LOCAL.info("Add cache. groupName:[{}] namespaceId:[{}] hostId:[{}]", serverNode.getGroupName(),
                    serverNode.getNamespaceId(), serverNode.getHostId());
            concurrentMap = new ConcurrentHashMap<>();
            registerNodeInfo = RegisterNodeInfoConverter.INSTANCE.toRegisterNodeInfo(serverNode);

        } else {
            // 复用缓存中的对象
            registerNodeInfo = concurrentMap.getOrDefault(serverNode.getHostId(),
                    RegisterNodeInfoConverter.INSTANCE.toRegisterNodeInfo(serverNode));
            registerNodeInfo.setExpireAt(serverNode.getExpireAt());

            // 删除过期的节点信息
            delExpireNode(concurrentMap);
        }

        concurrentMap.put(serverNode.getHostId(), registerNodeInfo);
        // 此缓存设置了60秒没有写入即过期，因此这次刷新缓存防止过期
        CACHE.put(serverNode.getGroupName(), concurrentMap);
    }

    /**
     * 删除过期的节点信息
     *
     * @param concurrentMap 并发映射的节点信息
     */
    private static void delExpireNode(ConcurrentMap<String, RegisterNodeInfo> concurrentMap) {
        concurrentMap.values().stream()
                .filter(registerNodeInfo -> registerNodeInfo.getExpireAt().isBefore(
                        Instant.now().minusSeconds(ServerRegister.DELAY_TIME + (ServerRegister.DELAY_TIME / 3))))
                .forEach(registerNodeInfo -> remove(registerNodeInfo.getGroupName(),
                        registerNodeInfo.getHostId()));
    }


    /**
     * 删除缓存
     *
     * @param groupName 组名称
     * @param hostId    机器id
     */
    public static void remove(String groupName, String hostId) {
        ConcurrentMap<String, RegisterNodeInfo> concurrentMap = CACHE.getIfPresent(groupName);
        if (Objects.isNull(concurrentMap)) {
            return;
        }

        SilenceJobLog.LOCAL.info("Remove cache. groupName:[{}] hostId:[{}]", groupName, hostId);
        concurrentMap.remove(hostId);
    }

    @Override
    public void start() {
        SilenceJobLog.LOCAL.info("CacheRegisterTable start");
    }


    @Override
    public void close() {
        SilenceJobLog.LOCAL.info("CacheRegisterTable stop");
        CACHE.invalidateAll();
    }
}
