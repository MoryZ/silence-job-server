package com.old.silence.job.server.dto;


import java.math.BigInteger;

public class JobNotifyConfigQueryVO  {
    private String groupName;
    private BigInteger jobId;


    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public BigInteger getJobId() {
        return jobId;
    }

    public void setJobId(BigInteger jobId) {
        this.jobId = jobId;
    }
}
