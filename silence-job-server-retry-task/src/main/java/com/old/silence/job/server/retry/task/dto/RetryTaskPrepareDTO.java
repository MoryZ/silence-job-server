package com.old.silence.job.server.retry.task.dto;


import com.old.silence.job.common.enums.RetryBlockStrategy;
import com.old.silence.job.common.enums.RetryTaskStatus;
import com.old.silence.job.common.enums.SystemTaskType;

import java.math.BigInteger;


public class RetryTaskPrepareDTO {

    private String namespaceId;

    private String groupName;

    private String sceneName;

    private BigInteger retryId;

    private BigInteger retryTaskId;

    private RetryTaskStatus taskStatus;

    private SystemTaskType taskType;

    private Long nextTriggerAt;

    private RetryBlockStrategy blockStrategy;

    private boolean onlyTimeoutCheck;

    private Integer executorTimeout;

    private Integer retryTaskExecutorScene;

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

    public BigInteger getRetryId() {
        return retryId;
    }

    public void setRetryId(BigInteger retryId) {
        this.retryId = retryId;
    }

    public BigInteger getRetryTaskId() {
        return retryTaskId;
    }

    public void setRetryTaskId(BigInteger retryTaskId) {
        this.retryTaskId = retryTaskId;
    }

    public RetryTaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(RetryTaskStatus taskStatus) {
        this.taskStatus = taskStatus;
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

    public RetryBlockStrategy getBlockStrategy() {
        return blockStrategy;
    }

    public void setBlockStrategy(RetryBlockStrategy blockStrategy) {
        this.blockStrategy = blockStrategy;
    }

    public boolean isOnlyTimeoutCheck() {
        return onlyTimeoutCheck;
    }

    public void setOnlyTimeoutCheck(boolean onlyTimeoutCheck) {
        this.onlyTimeoutCheck = onlyTimeoutCheck;
    }

    public Integer getExecutorTimeout() {
        return executorTimeout;
    }

    public void setExecutorTimeout(Integer executorTimeout) {
        this.executorTimeout = executorTimeout;
    }

    public Integer getRetryTaskExecutorScene() {
        return retryTaskExecutorScene;
    }

    public void setRetryTaskExecutorScene(Integer retryTaskExecutorScene) {
        this.retryTaskExecutorScene = retryTaskExecutorScene;
    }
}
