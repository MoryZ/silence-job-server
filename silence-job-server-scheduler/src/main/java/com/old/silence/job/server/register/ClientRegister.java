package com.old.silence.job.server.common.register;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.constant.SystemConstants.HTTP_PATH;
import com.old.silence.job.common.enums.NodeType;
import com.old.silence.job.common.model.ApiResult;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.cache.CacheConsumerGroup;
import com.old.silence.job.server.common.cache.CacheRegisterTable;
import com.old.silence.job.server.common.client.CommonRpcClient;
import com.old.silence.job.server.common.dto.PullRemoteNodeClientRegisterInfoDTO;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;
import com.old.silence.job.server.common.rpc.client.RequestBuilder;
import com.old.silence.job.server.common.schedule.AbstractSchedule;
import com.old.silence.job.server.domain.model.ServerNode;
import com.old.silence.job.server.infrastructure.persistence.dao.ServerNodeDao;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component(ClientRegister.BEAN_NAME)
public class ClientRegister extends AbstractRegister {
    public static final String BEAN_NAME = "clientRegister";
    public static final int DELAY_TIME = 30;
    protected static final LinkedBlockingDeque<ServerNode> QUEUE = new LinkedBlockingDeque<>(1000);

    @Autowired
    @Lazy
    private RefreshNodeSchedule refreshNodeSchedule;

    protected ClientRegister(ServerNodeDao serverNodeDao) {
        super(serverNodeDao);
    }

    @Override
    public boolean supports(NodeType type) {
        return getNodeType().equals(type);
    }

    @Override
    protected void beforeProcessor(RegisterContext context) {
    }

    @Override
    protected Instant getExpireAt() {
        return Instant.now().plusSeconds(DELAY_TIME);
    }

    @Override
    protected boolean doRegister(RegisterContext context, ServerNode serverNode) {
        if (HTTP_PATH.BEAT.equals(context.getUri())) {
            return QUEUE.offerFirst(serverNode);
        }

        return QUEUE.offerLast(serverNode);
    }

    @Override
    protected void afterProcessor(ServerNode serverNode) {
    }

    @Override
    protected NodeType getNodeType() {
        return NodeType.CLIENT;
    }

    @Override
    public void start() {
        refreshNodeSchedule.startScheduler();
    }

    @Override
    public void close() {
    }

    public static List<ServerNode> getExpireNodes() {

        ServerNode serverNode = QUEUE.poll();
        if (Objects.nonNull(serverNode)) {
            List<ServerNode> lists = new ArrayList<>();
            lists.add(serverNode);
            QUEUE.drainTo(lists, 256);
            return lists;
        }

        return null;
    }

    public static List<ServerNode> refreshLocalCache() {
        // 获取当前所有需要续签的node
        List<ServerNode> expireNodes = ClientRegister.getExpireNodes();
        if (Objects.nonNull(expireNodes)) {
            // 进行本地续签
            for (ServerNode serverNode : expireNodes) {
                serverNode.setExpireAt(Instant.now().plusSeconds(DELAY_TIME));
                // 刷新全量本地缓存
                CacheRegisterTable.addOrUpdate(serverNode);
                // 刷新过期时间
                CacheConsumerGroup.addOrUpdate(serverNode.getGroupName(), serverNode.getNamespaceId());
            }
        }
        return expireNodes;
    }

    @Component
    public class RefreshNodeSchedule extends AbstractSchedule {
        private ThreadPoolExecutor refreshNodePool;
        @Override
        protected void doExecute() {
            try {
                // 获取在线的客户端节点并且排除当前节点
                LambdaQueryWrapper<ServerNode> wrapper = new LambdaQueryWrapper<ServerNode>()
                        .eq(ServerNode::getNodeType, NodeType.SERVER);
                List<ServerNode> serverNodes = serverNodeDao.selectList(wrapper);

                serverNodes = StreamUtils.filter(serverNodes, serverNode -> !serverNode.getHostId().equals(ServerRegister.CURRENT_CID));

                List<ServerNode> waitRefreshDBClientNodes = new ArrayList<>();

                // 刷新本地缓存
                List<ServerNode> refreshCache = refreshLocalCache();
                if (CollectionUtils.isNotEmpty(refreshCache)) {
                    // 完成本节点的刷新
                    waitRefreshDBClientNodes.addAll(refreshCache);
                }

                if (!serverNodes.isEmpty()) {
                    // 并行获取所有服务端需要注册的列表
                    // 获取列表 并完成注册/本地完成续签
                    List<ServerNode> allClientList = pullRemoteNodeClientRegisterInfo(serverNodes);
                    if (CollectionUtils.isNotEmpty(allClientList)) {
                        waitRefreshDBClientNodes.addAll(allClientList);
                    }
                }

                if (CollectionUtils.isEmpty(waitRefreshDBClientNodes)) {
                    SilenceJobLog.LOCAL.debug("clientNodes is empty");
                    return;
                }

                SilenceJobLog.LOCAL.debug("start refresh client nodes：{}", waitRefreshDBClientNodes);

                // 刷新DB
                refreshExpireAt(waitRefreshDBClientNodes);

            } catch (Exception e) {
                SilenceJobLog.LOCAL.error("refresh 失败", e);
            }
        }

        private List<ServerNode> pullRemoteNodeClientRegisterInfo(List<ServerNode> serverNodes) {
            if (CollectionUtils.isEmpty(serverNodes)) {
                return Lists.newArrayList();
            }

            int size = serverNodes.size();
            // 存储处理结果
            List<Future<String>> futures = new ArrayList<>(size);
            for (ServerNode serverNode : serverNodes) {
                Future<String> future = refreshNodePool.submit(() -> {
                    try {
                        RegisterNodeInfo nodeInfo = new RegisterNodeInfo();
                        nodeInfo.setHostId(serverNode.getHostId());
                        nodeInfo.setGroupName(serverNode.getGroupName());
                        nodeInfo.setNamespaceId(serverNode.getNamespaceId());
                        nodeInfo.setHostPort(serverNode.getHostPort());
                        nodeInfo.setHostIp(serverNode.getHostIp());
                        CommonRpcClient serverRpcClient = buildRpcClient(nodeInfo);
                        ApiResult<String> regNodesAndFlush =
                                serverRpcClient.pullRemoteNodeClientRegisterInfo(new PullRemoteNodeClientRegisterInfoDTO());
                        return regNodesAndFlush.getData();
                    } catch (Exception e) {
                        return StrUtil.EMPTY;
                    }
                });

                futures.add(future);
            }

            return futures.stream()
                    .map(future -> {
                        try {
                            // 后面可以考虑配置
                            String jsonString = future.get(1, TimeUnit.SECONDS);
                            if (Objects.nonNull(jsonString)) {
                                return JSON.parseArray(
                                        jsonString, ServerNode.class);
                            }
                            return new ArrayList<ServerNode>();
                        } catch (Exception e) {
                            return new ArrayList<ServerNode>();
                        }
                    })
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .distinct()
                    .collect(Collectors.toList());

        }

        private CommonRpcClient buildRpcClient(RegisterNodeInfo registerNodeInfo) {

            int maxRetryTimes = 3;
            return RequestBuilder.<CommonRpcClient, ApiResult>newBuilder()
                    .nodeInfo(registerNodeInfo)
                    .failRetry(true)
                    .retryTimes(maxRetryTimes)
                    .client(CommonRpcClient.class)
                    .build();
        }

        @Override
        public String lockName() {
            return "registerNode";
        }

        @Override
        public String lockAtMost() {
            return "PT10S";
        }

        @Override
        public String lockAtLeast() {
            return "PT5S";
        }

        public void startScheduler() {
            // 后面可以考虑配置
            refreshNodePool = new ThreadPoolExecutor(4, 8, 1, TimeUnit.SECONDS,
                    new LinkedBlockingDeque<>(1000));
            refreshNodePool.allowCoreThreadTimeOut(true);
            taskScheduler.scheduleWithFixedDelay(this::execute, Instant.now(), Duration.parse("PT5S"));
        }
    }

}
