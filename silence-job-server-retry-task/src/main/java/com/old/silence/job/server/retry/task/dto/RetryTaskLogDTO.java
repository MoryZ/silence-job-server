package com.old.silence.job.server.retry.task.dto;


import com.old.silence.job.common.enums.RetryStatus;

import java.math.BigInteger;
import java.time.Instant;

/**
 * 日志上下文模型
 *
 */

public class RetryTaskLogDTO {

    /**
     * 命名空间
     */
    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 重试任务id
     */
    private BigInteger retryTaskId;

    /**
     * 重试信息Id
     */
    private BigInteger retryId;

    /**
     * 异常信息
     */
    private String message;

    /**
     * 重试状态
     */
    private RetryStatus retryStatus;

    /**
     * 触发时间
     */
    private Instant triggerTime;

    /**
     * 客户端信息
     */
    private String clientInfo;

    /**
     * 真实上报时间
     */
    private Long realTime;

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

    public BigInteger getRetryTaskId() {
        return retryTaskId;
    }

    public void setRetryTaskId(BigInteger retryTaskId) {
        this.retryTaskId = retryTaskId;
    }

    public BigInteger getRetryId() {
        return retryId;
    }

    public void setRetryId(BigInteger retryId) {
        this.retryId = retryId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RetryStatus getRetryStatus() {
        return retryStatus;
    }

    public void setRetryStatus(RetryStatus retryStatus) {
        this.retryStatus = retryStatus;
    }

    public Instant getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(Instant triggerTime) {
        this.triggerTime = triggerTime;
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(String clientInfo) {
        this.clientInfo = clientInfo;
    }

    public Long getRealTime() {
        return realTime;
    }

    public void setRealTime(Long realTime) {
        this.realTime = realTime;
    }
}
