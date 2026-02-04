package com.old.silence.job.server.common.handler;


import org.springframework.stereotype.Component;
import com.old.silence.job.common.model.ApiResult;
import com.old.silence.job.common.dto.ConfigDTO;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;
import com.old.silence.job.server.common.cache.CacheRegisterTable;
import com.old.silence.job.server.common.client.CommonRpcClient;
import com.old.silence.job.server.common.dto.ConfigSyncTask;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;
import com.old.silence.job.server.common.rpc.client.RequestBuilder;
import com.old.silence.job.server.domain.service.AccessTemplate;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 版本同步
 *
 */
@Component
public class ConfigVersionSyncHandler implements Lifecycle, Runnable {
    private static final LinkedBlockingQueue<ConfigSyncTask> QUEUE = new LinkedBlockingQueue<>(256);
    public Thread THREAD = null;
    protected final AccessTemplate accessTemplate;

    public ConfigVersionSyncHandler(AccessTemplate accessTemplate) {
        this.accessTemplate = accessTemplate;
    }

    /**
     * 添加任务
     *
     * @param groupName      组
     * @param currentVersion 当前版本号
     * @return false-队列容量已满， true-添加成功
     */
    public boolean addSyncTask(String groupName, String namespaceId, Integer currentVersion) {

        ConfigSyncTask configSyncTask = new ConfigSyncTask();
        configSyncTask.setCurrentVersion(currentVersion);
        configSyncTask.setGroupName(groupName);
        configSyncTask.setNamespaceId(namespaceId);
        return QUEUE.offer(configSyncTask);
    }

    /**
     * 同步版本
     *
     * @param groupName   组
     */
    public void syncVersion(String groupName, String namespaceId) {

        try {
            Set<RegisterNodeInfo> serverNodeSet = CacheRegisterTable.getServerNodeSet(groupName, namespaceId);
            // 同步版本到每个客户端节点
            for (RegisterNodeInfo registerNodeInfo : serverNodeSet) {
                ConfigDTO configDTO = accessTemplate.getGroupConfigAccess().getConfigInfo(groupName, namespaceId);
                CommonRpcClient rpcClient = RequestBuilder.<CommonRpcClient, ApiResult>newBuilder()
                        .nodeInfo(registerNodeInfo)
                        .client(CommonRpcClient.class)
                        .build();
                SilenceJobLog.LOCAL.info("同步结果 [{}]", rpcClient.syncConfig(configDTO));
            }
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("version sync error. groupName:[{}]", groupName, e);
        }
    }

    @Override
    public void start() {
        THREAD = new Thread(this, "config-version-sync");
        THREAD.start();
    }

    @Override
    public void close() {
        if (Objects.nonNull(THREAD)) {
            THREAD.interrupt();
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ConfigSyncTask task = QUEUE.take();
                // 远程版本号
                Integer remoteVersion = accessTemplate.getGroupConfigAccess().getConfigVersion(task.getGroupName(), task.getNamespaceId());
                if (Objects.isNull(remoteVersion) || !task.getCurrentVersion().equals(remoteVersion)) {
                    syncVersion(task.getGroupName(), task.getNamespaceId());
                }
            } catch (InterruptedException e) {
                SilenceJobLog.LOCAL.info("[{}] thread stop.", Thread.currentThread().getName());
            } catch (Exception e) {
                SilenceJobLog.LOCAL.error("client refresh expireAt error.", e);
            } finally {
                try {
                    // 防止刷的过快，休眠1s
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
