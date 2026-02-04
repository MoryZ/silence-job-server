package com.old.silence.job.server.dto;


import java.math.BigInteger;

public class RetryTaskLogMessageQueryVO  {

    private String groupName;

    private BigInteger retryTaskId;

    private BigInteger startId;

    private Integer fromIndex;

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

    public BigInteger getStartId() {
        return startId;
    }

    public void setStartId(BigInteger startId) {
        this.startId = startId;
    }

    public Integer getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(Integer fromIndex) {
        this.fromIndex = fromIndex;
    }
}
