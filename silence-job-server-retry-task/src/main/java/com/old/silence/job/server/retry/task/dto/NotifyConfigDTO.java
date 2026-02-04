package com.old.silence.job.server.retry.task.dto;


import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.NotifyType;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;


public class NotifyConfigDTO {

    private BigInteger id;

    private Set<BigInteger> recipientIds;

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

    public Set<BigInteger> getRecipientIds() {
        return recipientIds;
    }

    public void setRecipientIds(Set<BigInteger> recipientIds) {
        this.recipientIds = recipientIds;
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
