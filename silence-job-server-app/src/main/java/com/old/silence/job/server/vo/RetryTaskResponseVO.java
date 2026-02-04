package com.old.silence.job.server.vo;


import com.old.silence.job.common.enums.RetryOperationReason;
import com.old.silence.job.common.enums.RetryTaskStatus;
import com.old.silence.job.common.enums.SystemTaskType;

import java.math.BigInteger;
import java.time.Instant;



public class RetryTaskResponseVO {

    private BigInteger id;

    private String groupName;

    private String sceneName;

    private RetryTaskStatus taskStatus;

    private BigInteger retryId;

    private SystemTaskType taskType;

    private Instant createdDate;

    private RetryOperationReason operationReason;

    /**
     * 客户端ID
     */
    private String clientInfo;

    private RetryResponseVO responseVO;

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

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public RetryTaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(RetryTaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public BigInteger getRetryId() {
        return retryId;
    }

    public void setRetryId(BigInteger retryId) {
        this.retryId = retryId;
    }

    public SystemTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(SystemTaskType taskType) {
        this.taskType = taskType;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public RetryOperationReason getOperationReason() {
        return operationReason;
    }

    public void setOperationReason(RetryOperationReason operationReason) {
        this.operationReason = operationReason;
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(String clientInfo) {
        this.clientInfo = clientInfo;
    }

    public RetryResponseVO getResponseVO() {
        return responseVO;
    }

    public void setResponseVO(RetryResponseVO responseVO) {
        this.responseVO = responseVO;
    }
}
