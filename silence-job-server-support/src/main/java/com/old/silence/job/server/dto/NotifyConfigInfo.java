package com.old.silence.job.server.common.dto;



import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.NotifyType;
import com.old.silence.job.common.enums.SystemTaskType;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;



public class NotifyConfigInfo {

    private BigInteger id;

    private String namespaceId;

    private String groupName;

    // 业务id (scene_name或job_id或workflow_id)
    private String businessId;

    private Set<BigInteger> recipientIds;

    // 任务类型 1、重试任务 2、回调任务、 3、JOB任务 4、WORKFLOW任务
    private SystemTaskType systemTaskType;

    private Boolean notifyStatus;

    private Integer notifyThreshold;

    private JobNotifyScene notifyScene;

    private Boolean rateLimiterStatus;

    private Integer rateLimiterThreshold;

    private List<RecipientInfo> recipientInfos;

    
    public static class RecipientInfo {

        private NotifyType notifyType;

        private String notifyAttribute;

        public NotifyType getNotifyType() {
            return notifyType;
        }

        public void setNotifyType(NotifyType notifyType) {
            this.notifyType = notifyType;
        }

        public String getNotifyAttribute() {
            return notifyAttribute;
        }

        public void setNotifyAttribute(String notifyAttribute) {
            this.notifyAttribute = notifyAttribute;
        }
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

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

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public Set<BigInteger> getRecipientIds() {
        return recipientIds;
    }

    public void setRecipientIds(Set<BigInteger> recipientIds) {
        this.recipientIds = recipientIds;
    }

    public SystemTaskType getSystemTaskType() {
        return systemTaskType;
    }

    public void setSystemTaskType(SystemTaskType systemTaskType) {
        this.systemTaskType = systemTaskType;
    }

    public Boolean getNotifyStatus() {
        return notifyStatus;
    }

    public void setNotifyStatus(Boolean notifyStatus) {
        this.notifyStatus = notifyStatus;
    }

    public Integer getNotifyThreshold() {
        return notifyThreshold;
    }

    public void setNotifyThreshold(Integer notifyThreshold) {
        this.notifyThreshold = notifyThreshold;
    }

    public JobNotifyScene getNotifyScene() {
        return notifyScene;
    }

    public void setNotifyScene(JobNotifyScene notifyScene) {
        this.notifyScene = notifyScene;
    }

    public Boolean getRateLimiterStatus() {
        return rateLimiterStatus;
    }

    public void setRateLimiterStatus(Boolean rateLimiterStatus) {
        this.rateLimiterStatus = rateLimiterStatus;
    }

    public Integer getRateLimiterThreshold() {
        return rateLimiterThreshold;
    }

    public void setRateLimiterThreshold(Integer rateLimiterThreshold) {
        this.rateLimiterThreshold = rateLimiterThreshold;
    }

    public List<RecipientInfo> getRecipientInfos() {
        return recipientInfos;
    }

    public void setRecipientInfos(List<RecipientInfo> recipientInfos) {
        this.recipientInfos = recipientInfos;
    }
}
