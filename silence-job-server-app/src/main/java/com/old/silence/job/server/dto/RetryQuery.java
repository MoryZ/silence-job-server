package com.old.silence.job.server.dto;


import com.old.silence.data.commons.annotation.RelationalQueryProperty;
import com.old.silence.data.commons.converter.Part;
import com.old.silence.job.common.enums.RetryStatus;

import java.math.BigInteger;

public class RetryQuery {

    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private String groupName;

    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private String sceneName;

    @RelationalQueryProperty(type = Part.Type.CONTAINING)
    private String bizNo;

    @RelationalQueryProperty(type = Part.Type.CONTAINING)
    private String idempotentId;

    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private RetryStatus retryStatus;

    @RelationalQueryProperty(name = "id", type = Part.Type.SIMPLE_PROPERTY)
    private BigInteger retryId;

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

    public String getBizNo() {
        return bizNo;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }

    public String getIdempotentId() {
        return idempotentId;
    }

    public void setIdempotentId(String idempotentId) {
        this.idempotentId = idempotentId;
    }

    public RetryStatus getRetryStatus() {
        return retryStatus;
    }

    public void setRetryStatus(RetryStatus retryStatus) {
        this.retryStatus = retryStatus;
    }

    public BigInteger getRetryId() {
        return retryId;
    }

    public void setRetryId(BigInteger retryId) {
        this.retryId = retryId;
    }
}
