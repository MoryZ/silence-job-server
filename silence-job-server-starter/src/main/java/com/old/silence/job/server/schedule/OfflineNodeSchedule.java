package com.old.silence.job.server.schedule;

import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;
import com.old.silence.job.server.common.cache.CacheRegisterTable;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;
import com.old.silence.job.server.common.register.ServerRegister;
import com.old.silence.job.server.common.schedule.AbstractSchedule;
import com.old.silence.job.server.domain.model.ServerNode;
import com.old.silence.job.server.infrastructure.persistence.dao.ServerNodeDao;


import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 删除过期下线机器
 *
 */
@Component
public class OfflineNodeSchedule extends AbstractSchedule implements Lifecycle {
    private final ServerNodeDao serverNodeDao;

    public OfflineNodeSchedule(ServerNodeDao serverNodeDao) {
        this.serverNodeDao = serverNodeDao;
    }

    @Override
    protected void doExecute() {

        try {
            // 删除内存缓存的待下线的机器
            Instant endTime = Instant.now().minusSeconds(
                    ServerRegister.DELAY_TIME + (ServerRegister.DELAY_TIME / 3));

            List<ServerNode> serverNodes = serverNodeDao.selectList(
                    new LambdaQueryWrapper<ServerNode>().select(ServerNode::getId)
                            .le(ServerNode::getExpireAt, endTime));
            if (CollectionUtils.isNotEmpty(serverNodes)) {
                // 先删除DB中需要下线的机器
                serverNodeDao.deleteBatchIds(StreamUtils.toSet(serverNodes, ServerNode::getId));
            }

            Set<RegisterNodeInfo> allPods = CacheRegisterTable.getAllPods();
            Set<RegisterNodeInfo> waitOffline = allPods.stream().filter(registerNodeInfo -> registerNodeInfo.getExpireAt().isBefore(endTime)).collect(
                    Collectors.toSet());
            Set<String> podIds = StreamUtils.toSet(waitOffline, RegisterNodeInfo::getHostId);
            if (CollectionUtils.isEmpty(podIds)) {
                return;
            }

            for (RegisterNodeInfo registerNodeInfo : waitOffline) {
                CacheRegisterTable.remove(registerNodeInfo.getGroupName(), registerNodeInfo.getHostId());
            }

        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("clearOfflineNode 失败", e);
        }
    }

    @Override
    public String lockName() {
        return "clearOfflineNode";
    }

    @Override
    public String lockAtMost() {
        return "PT10S";
    }

    @Override
    public String lockAtLeast() {
        return "PT5S";
    }

    @Override
    public void start() {
        taskScheduler.scheduleWithFixedDelay(this::execute, Instant.now(), Duration.parse("PT5S"));
    }

    @Override
    public void close() {

    }
}
