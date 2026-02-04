package com.old.silence.job.server.dto;


import com.old.silence.data.commons.annotation.RelationalQueryProperty;
import com.old.silence.data.commons.converter.Part;
import com.old.silence.job.common.enums.JobTaskStatus;
import com.old.silence.job.common.enums.RetryTaskStatus;

import java.math.BigInteger;
import java.time.Instant;



public class RetryTaskQuery {

    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private String groupName;

    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private String sceneName;

    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private BigInteger retryId;

    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private RetryTaskStatus taskStatus;

    @RelationalQueryProperty(name = "createdDate", type = Part.Type.GREATER_THAN_EQUAL)
    private Instant createdDateStart;

    @RelationalQueryProperty(name = "createdDate", type = Part.Type.LESS_THAN_EQUAL)
    private Instant createdDateEnd;

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

    public RetryTaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(RetryTaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Instant getCreatedDateStart() {
        return createdDateStart;
    }

    public void setCreatedDateStart(Instant createdDateStart) {
        this.createdDateStart = createdDateStart;
    }

    public Instant getCreatedDateEnd() {
        return createdDateEnd;
    }

    public void setCreatedDateEnd(Instant createdDateEnd) {
        this.createdDateEnd = createdDateEnd;
    }
}
