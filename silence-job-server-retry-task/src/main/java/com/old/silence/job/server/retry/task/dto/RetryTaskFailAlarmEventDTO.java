package com.old.silence.job.server.retry.task.dto;


import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.RetryStatus;
import com.old.silence.job.common.enums.SystemTaskType;

import java.math.BigInteger;


public class RetryTaskFailAlarmEventDTO {

    private BigInteger id;

    private String namespaceId;

    private String uniqueId;

    private String groupName;

    private String sceneName;

    private String idempotentId;

    private String bizNo;

    private String argsStr;

    private String extAttrs;

    private String executorName;

    private Long nextTriggerAt;

    private Integer retryCount;

    private RetryStatus retryStatus;

    private SystemTaskType taskType;

    private JobNotifyScene notifyScene;

    private String reason;

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

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
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

    public String getIdempotentId() {
        return idempotentId;
    }

    public void setIdempotentId(String idempotentId) {
        this.idempotentId = idempotentId;
    }

    public String getBizNo() {
        return bizNo;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }

    public String getArgsStr() {
        return argsStr;
    }

    public void setArgsStr(String argsStr) {
        this.argsStr = argsStr;
    }

    public String getExtAttrs() {
        return extAttrs;
    }

    public void setExtAttrs(String extAttrs) {
        this.extAttrs = extAttrs;
    }

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }

    public Long getNextTriggerAt() {
        return nextTriggerAt;
    }

    public void setNextTriggerAt(Long nextTriggerAt) {
        this.nextTriggerAt = nextTriggerAt;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public RetryStatus getRetryStatus() {
        return retryStatus;
    }

    public void setRetryStatus(RetryStatus retryStatus) {
        this.retryStatus = retryStatus;
    }

    public SystemTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(SystemTaskType taskType) {
        this.taskType = taskType;
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
