package com.old.silence.job.server.retry.task.dto;

import com.old.silence.job.server.common.dto.PartitionTask;

import java.math.BigInteger;
import java.util.Set;


public class RetrySceneConfigPartitionTask extends PartitionTask {

    private String namespaceId;

    private String groupName;

    private String sceneName;

    /**
     * 通知告警场景配置id列表
     */
    private Set<BigInteger> notifyIds;

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public Set<BigInteger> getNotifyIds() {
        return notifyIds;
    }

    public void setNotifyIds(Set<BigInteger> notifyIds) {
        this.notifyIds = notifyIds;
    }
}
