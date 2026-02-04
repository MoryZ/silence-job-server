package com.old.silence.job.server.common.register;

import org.springframework.dao.DuplicateKeyException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Sets;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.NodeType;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;
import com.old.silence.job.server.common.Register;
import com.old.silence.job.server.common.cache.CacheRegisterTable;
import com.old.silence.job.server.common.triple.Pair;
import com.old.silence.job.server.domain.model.ServerNode;
import com.old.silence.job.server.infrastructure.persistence.dao.ServerNodeDao;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;



public abstract class AbstractRegister implements Register, Lifecycle {

    protected final ServerNodeDao serverNodeDao;

    protected AbstractRegister(ServerNodeDao serverNodeDao) {
        this.serverNodeDao = serverNodeDao;
    }

    @Override
    public boolean register(RegisterContext context) {

        beforeProcessor(context);

        ServerNode serverNode = initServerNode(context);

        boolean result = doRegister(context, serverNode);

        afterProcessor(serverNode);

        return result;
    }

    protected abstract void afterProcessor(ServerNode serverNode);

    protected void refreshExpireAt(List<ServerNode> serverNodes) {
        if (CollectionUtils.isEmpty(serverNodes)) {
            return;
        }

        Set<String> hostIds = Sets.newHashSet();
        Set<String> hostIps = Sets.newHashSet();
        for (ServerNode serverNode : serverNodes) {
            serverNode.setExpireAt(getExpireAt());
             hostIds.add(serverNode.getHostId());
             hostIps.add(serverNode.getHostIp());
        }

        List<ServerNode> dbServerNodes = serverNodeDao.selectList(
            new LambdaQueryWrapper<ServerNode>()
                .select(ServerNode::getHostIp, ServerNode::getHostId)
                .in(ServerNode::getHostId, hostIds)
                .in(ServerNode::getHostIp, hostIps)
        );

        List<ServerNode> insertDBs = new ArrayList<>();
        List<ServerNode> updateDBs = new ArrayList<>();
        Set<Pair<String, String>> pairs = dbServerNodes.stream()
            .map(serverNode -> Pair.of(serverNode.getHostId(), serverNode.getHostIp())).collect(
                Collectors.toSet());

        // 去重处理
        Set<Pair<String, String>> existed = Sets.newHashSet();
        for (ServerNode serverNode : serverNodes) {
            Pair<String, String> pair = Pair.of(serverNode.getHostId(), serverNode.getHostIp());
            if (existed.contains(pair)) {
                continue;
            }

            if (pairs.contains(pair)) {
                updateDBs.add(serverNode);
            } else {
                insertDBs.add(serverNode);
            }

            existed.add(pair);
        }

        try {
            // 批量更新
            if (CollectionUtils.isNotEmpty(updateDBs)) {
                serverNodeDao.updateBatchExpireAt(updateDBs);
            }
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("续租失败", e);
        }

        try {
            if (CollectionUtils.isNotEmpty(insertDBs)) {
                serverNodeDao.insertBatch(insertDBs);
            }
        } catch (DuplicateKeyException ignored) {
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("注册节点失败", e);
        }

        for (ServerNode serverNode : serverNodes) {
            // 刷新本地缓存过期时间
            CacheRegisterTable.refreshExpireAt(serverNode);
        }

    }

    protected abstract void beforeProcessor(RegisterContext context);

    protected ServerNode initServerNode(RegisterContext context) {

        ServerNode serverNode = new ServerNode();
        serverNode.setHostId(context.getHostId());
        serverNode.setHostIp(context.getHostIp());
        serverNode.setNamespaceId(context.getNamespaceId());
        serverNode.setGroupName(context.getGroupName());
        serverNode.setHostPort(context.getHostPort());
        serverNode.setNodeType(getNodeType());
        serverNode.setCreatedDate(Instant.now());
        serverNode.setExtAttrs(context.getExtAttrs());

        return serverNode;
    }

    protected abstract Instant getExpireAt();


    protected abstract boolean doRegister(RegisterContext context, ServerNode serverNode);


    protected abstract NodeType getNodeType();


}
