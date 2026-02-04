package com.old.silence.job.server.retry.task.dto;

import com.old.silence.job.common.enums.RetryStatus;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.server.common.dto.PartitionTask;

import java.math.BigInteger;


public class RetryPartitionTask extends PartitionTask {

    private String namespaceId;

    private String groupName;

    private String sceneName;

    private SystemTaskType taskType;

    /**
     * 下次触发时间
     */
    private Long nextTriggerAt;

    private Integer retryCount;

    private String idempotentId;

    private String bizNo;

    private String argsStr;

    private String extAttrs;

    private String executorName;

    private RetryStatus retryStatus;

    private BigInteger parentId;

    private Integer bucketIndex;

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

    public SystemTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(SystemTaskType taskType) {
        this.taskType = taskType;
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

    public RetryStatus getRetryStatus() {
        return retryStatus;
    }

    public void setRetryStatus(RetryStatus retryStatus) {
        this.retryStatus = retryStatus;
    }

    public BigInteger getParentId() {
        return parentId;
    }

    public void setParentId(BigInteger parentId) {
        this.parentId = parentId;
    }

    public Integer getBucketIndex() {
        return bucketIndex;
    }

    public void setBucketIndex(Integer bucketIndex) {
        this.bucketIndex = bucketIndex;
    }
}
