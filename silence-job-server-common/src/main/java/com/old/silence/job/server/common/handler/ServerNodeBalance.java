package com.old.silence.job.server.common.handler;

import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.NodeType;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;
import com.old.silence.job.server.common.allocate.server.AllocateMessageQueueAveragely;
import com.old.silence.job.server.common.cache.CacheRegisterTable;
import com.old.silence.job.server.common.config.SystemProperties;
import com.old.silence.job.server.common.dto.DistributeInstance;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;
import com.old.silence.job.server.common.register.ServerRegister;
import com.old.silence.job.server.domain.model.ServerNode;
import com.old.silence.job.server.infrastructure.persistence.dao.ServerNodeDao;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Component

public class ServerNodeBalance implements Lifecycle, Runnable {

    /**
     * 延迟10s为了尽可能保障集群节点都启动完成在进行 reBalance
     */
    public static final Long INITIAL_DELAY = 10L;
    private final ServerNodeDao serverNodeDao;
    private final SystemProperties systemProperties;

    private Thread thread = null;
    private List<Integer> bucketList;

    public ServerNodeBalance(ServerNodeDao serverNodeDao, SystemProperties systemProperties) {
        this.serverNodeDao = serverNodeDao;
        this.systemProperties = systemProperties;
    }

    public void doBalance() {
        SilenceJobLog.LOCAL.info("rebalance start");
        DistributeInstance.RE_BALANCE_ING.set(Boolean.TRUE);

        try {

            // 为了保证客户端分配算法的一致性,serverNodes 从数据库从数据获取
            Set<String> podIpSet = CacheRegisterTable.getPodIdSet(ServerRegister.GROUP_NAME);

            if (CollectionUtils.isEmpty(podIpSet)) {
                SilenceJobLog.LOCAL.error("server node is empty");
            }

            // 删除本地缓存的消费桶的信息
            DistributeInstance.INSTANCE.clearConsumerBucket();
            if (CollectionUtils.isEmpty(podIpSet)) {
                return;
            }

            List<Integer> allocate = new AllocateMessageQueueAveragely()
                    .allocate(ServerRegister.CURRENT_CID, bucketList, new ArrayList<>(podIpSet));

            // 重新覆盖本地分配的bucket
            DistributeInstance.INSTANCE.setConsumerBucket(allocate);

            SilenceJobLog.LOCAL.info("rebalance complete. allocate:[{}]", allocate);
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("rebalance error. ", e);
        } finally {
            DistributeInstance.RE_BALANCE_ING.set(Boolean.FALSE);
        }

    }

    @Override
    public void start() {

        int bucketTotal = systemProperties.getBucketTotal();
        bucketList = new ArrayList<>(bucketTotal);
        for (int i = 0; i < bucketTotal; i++) {
            bucketList.add(i);
        }

        SilenceJobLog.LOCAL.info("ServerNodeBalance start");
        thread = new Thread(this, "server-node-balance");
        thread.start();
    }

    private void removeNode(ConcurrentMap<String, RegisterNodeInfo> concurrentMap, Set<String> remoteHostIds, Set<String> localHostIds) {

        localHostIds.removeAll(remoteHostIds);
        for (String localHostId : localHostIds) {
            RegisterNodeInfo registerNodeInfo = concurrentMap.get(localHostId);
            // 删除过期的节点信息
            CacheRegisterTable.remove(registerNodeInfo.getGroupName(), registerNodeInfo.getHostId());
        }
    }

    private void refreshExpireAtCache(List<ServerNode> remotePods) {
        // 重新刷新缓存
        refreshCache(remotePods);
    }

    private void refreshCache(List<ServerNode> remotePods) {

        // 刷新最新的节点注册信息
        for (ServerNode node : remotePods) {
            CacheRegisterTable.addOrUpdate(node);
        }
    }

    @Override
    public void close() {

        // 停止定时任务
        thread.interrupt();

        SilenceJobLog.LOCAL.info("ServerNodeBalance start. ");
        int i = serverNodeDao
                .delete(new LambdaQueryWrapper<ServerNode>().eq(ServerNode::getHostId, ServerRegister.CURRENT_CID));
        if (1 == i) {
            SilenceJobLog.LOCAL.info("delete node success. [{}]", ServerRegister.CURRENT_CID);
        } else {
            SilenceJobLog.LOCAL.info("delete node  error. [{}]", ServerRegister.CURRENT_CID);
        }

        SilenceJobLog.LOCAL.info("ServerNodeBalance close complete");
    }

    @Override
    public void run() {
        try {
            TimeUnit.SECONDS.sleep(INITIAL_DELAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        while (!Thread.currentThread().isInterrupted()) {
            try {

                List<ServerNode> remotePods = serverNodeDao.selectList(new LambdaQueryWrapper<ServerNode>()
                        .ge(ServerNode::getExpireAt, Instant.now())
                        .eq(ServerNode::getNodeType, NodeType.SERVER));

                // 获取缓存中的节点
                ConcurrentMap<String/*hostId*/, RegisterNodeInfo> concurrentMap = Optional.ofNullable(CacheRegisterTable
                        .get(ServerRegister.GROUP_NAME)).orElse(new ConcurrentHashMap<>());

                Set<String> remoteHostIds = StreamUtils.toSet(remotePods, ServerNode::getHostId);

                Set<String> localHostIds = StreamUtils.toSet(concurrentMap.values(), RegisterNodeInfo::getHostId);

                // 无缓存的节点触发refreshCache
                if (CollectionUtils.isEmpty(concurrentMap)
                        // 节点数量不一致触发
                        || isNodeSizeNotEqual(concurrentMap.size(), remotePods.size())
                        // 判断远程节点是不是和本地节点一致的，如果不一致则重新分配
                        || isNodeNotMatch(remoteHostIds, localHostIds)
                        // 检查当前节点的消费桶是否为空，为空则重新负载
                        || checkConsumerBucket(remoteHostIds)
                ) {

                    // 删除本地缓存以下线的节点信息
                    removeNode(concurrentMap, remoteHostIds, localHostIds);

                    // 重新获取DB中最新的服务信息
                    refreshCache(remotePods);

                    // 触发rebalance
                    doBalance();

                    // 每次rebalance之后给10秒作为空闲时间，等待其他的节点也完成rebalance
                    TimeUnit.SECONDS.sleep(INITIAL_DELAY);

                } else {

                    // 刷新过期时间
                    refreshExpireAtCache(remotePods);

                    // 再次获取最新的节点信息
                    concurrentMap = CacheRegisterTable
                            .get(ServerRegister.GROUP_NAME);

                    // 找出过期的节点
                    Set<RegisterNodeInfo> expireNodeSet = concurrentMap.values().stream()
                            .filter(registerNodeInfo -> registerNodeInfo.getExpireAt().isBefore(Instant.now()))
                            .collect(Collectors.toSet());
                    for (RegisterNodeInfo registerNodeInfo : expireNodeSet) {
                        // 删除过期的节点信息
                        CacheRegisterTable.remove(registerNodeInfo.getGroupName(), registerNodeInfo.getHostId());
                    }

                }

            } catch (InterruptedException e) {
                SilenceJobLog.LOCAL.info("check balance stop");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                SilenceJobLog.LOCAL.error("check balance error", e);
            } finally {
                try {
                    TimeUnit.SECONDS.sleep(systemProperties.getLoadBalanceCycleTime());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

    }

    private boolean isNodeNotMatch(Set<String> remoteHostIds, Set<String> localHostIds) {
        boolean b = !remoteHostIds.containsAll(localHostIds);
        if (b) {
            SilenceJobLog.LOCAL.info("判断远程节点是不是和本地节点一致. remoteHostIds:[{}] localHostIds:[{}]",
                    localHostIds,
                    remoteHostIds);
        }

        // 若在线节点小于总的Bucket数量且当前节点无任何分桶，则需要重新负载
        if (CollectionUtils.isEmpty(DistributeInstance.INSTANCE.getConsumerBucket()) && remoteHostIds.size() <= systemProperties.getBucketTotal()) {
            return true;
        }

        return b;
    }

    public boolean checkConsumerBucket(Set<String> remoteHostIds) {
        return CollectionUtils.isEmpty(DistributeInstance.INSTANCE.getConsumerBucket()) && remoteHostIds.size() <= systemProperties.getBucketTotal();
    }

    private boolean isNodeSizeNotEqual(int localNodeSize, int remoteNodeSize) {
        boolean b = localNodeSize != remoteNodeSize;
        if (b) {
            SilenceJobLog.LOCAL.info("存在远程和本地缓存的节点的数量不一致则触发rebalance. localNodeSize:[{}] remoteNodeSize:[{}]",
                    localNodeSize,
                    remoteNodeSize);
        }
        return b;
    }

}
