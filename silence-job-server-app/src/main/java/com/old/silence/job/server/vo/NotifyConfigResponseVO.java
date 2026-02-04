package com.old.silence.job.server.vo;


import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.SystemTaskType;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Set;


public class NotifyConfigResponseVO implements Serializable {

    private BigInteger id;

    private String groupName;

    /**
     * 业务id (scene_name或job_id或workflow_id)
     */
    private String businessId;

    private String businessName;

    /**
     * 任务类型 1、重试任务 2、回调任务、 3、JOB任务 4、WORKFLOW任务
     */
    private SystemTaskType systemTaskType;

    private Boolean notifyStatus;

    private String notifyName;

    private Set<BigInteger> recipientIds;

    private Set<String> recipientNames;

    private Integer notifyThreshold;

    private JobNotifyScene notifyScene;

    private Boolean rateLimiterStatus;

    private Integer rateLimiterThreshold;

    private String description;

    private Instant createdDate;

    private Instant updatedDate;


    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
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

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
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

    public String getNotifyName() {
        return notifyName;
    }

    public void setNotifyName(String notifyName) {
        this.notifyName = notifyName;
    }

    public Set<BigInteger> getRecipientIds() {
        return recipientIds;
    }

    public void setRecipientIds(Set<BigInteger> recipientIds) {
        this.recipientIds = recipientIds;
    }

    public Set<String> getRecipientNames() {
        return recipientNames;
    }

    public void setRecipientNames(Set<String> recipientNames) {
        this.recipientNames = recipientNames;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    private static final long serialVersionUID = 1L;


}
