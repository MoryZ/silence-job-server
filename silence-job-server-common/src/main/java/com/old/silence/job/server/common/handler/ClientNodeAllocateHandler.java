package com.old.silence.job.server.common.handler;

import org.springframework.stereotype.Component;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.ClientLoadBalance;
import com.old.silence.job.server.common.allocate.client.ClientLoadBalanceManager;
import com.old.silence.job.server.common.cache.CacheRegisterTable;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;


@Component
public class ClientNodeAllocateHandler {

    /**
     * 获取分配的节点
     *
     * @param allocKey  分配的key
     * @param groupName 组名称
     * @param routeKey  {@link ClientLoadBalanceManager.AllocationAlgorithmEnum} 路由类型
     */
    public RegisterNodeInfo getServerNode(String allocKey, String groupName, String namespaceId, Integer routeKey) {

        Set<RegisterNodeInfo> serverNodes = CacheRegisterTable.getServerNodeSet(groupName, namespaceId);
        if (CollectionUtils.isEmpty(serverNodes)) {
            SilenceJobLog.LOCAL.warn("client node is null. groupName:[{}]", groupName);
            return null;
        }

        ClientLoadBalance clientLoadBalanceRandom = ClientLoadBalanceManager.getClientLoadBalance(routeKey);

        String hostId = clientLoadBalanceRandom.route(allocKey, new TreeSet<>(StreamUtils.toSet(serverNodes, RegisterNodeInfo::getHostId)));

        Stream<RegisterNodeInfo> registerNodeInfoStream = serverNodes.stream()
                .filter(s -> s.getHostId().equals(hostId));
        return registerNodeInfoStream.findFirst().orElse(null);
    }

}
