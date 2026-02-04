package com.old.silence.job.server.common.dto;


import com.old.silence.job.common.enums.JobNotifyScene;

public class AlarmInfo {

    private String namespaceId;

    private String groupName;

    private Integer count;

    /**
     * 通知配置
     */
    private String notifyIds;

    /**
     * 通知场景
     */
    private JobNotifyScene notifyScene;

    /**
     * 失败原因
     */
    private String reason;

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

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getNotifyIds() {
        return notifyIds;
    }

    public void setNotifyIds(String notifyIds) {
        this.notifyIds = notifyIds;
    }

    public JobNotifyScene getNotifyScene() {
        return notifyScene;
    }

    public void setNotifyScene(JobNotifyScene notifyScene) {
        this.notifyScene = notifyScene;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
