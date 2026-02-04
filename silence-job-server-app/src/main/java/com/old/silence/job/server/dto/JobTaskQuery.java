package com.old.silence.job.server.dto;


import com.old.silence.job.common.enums.JobTaskStatus;

import java.math.BigInteger;

public class JobTaskQuery {
    private BigInteger jobId;
    private BigInteger taskBatchId;
    private BigInteger parentId;
    private JobTaskStatus taskStatus;

    public BigInteger getJobId() {
        return jobId;
    }

    public void setJobId(BigInteger jobId) {
        this.jobId = jobId;
    }

    public BigInteger getTaskBatchId() {
        return taskBatchId;
    }

    public void setTaskBatchId(BigInteger taskBatchId) {
        this.taskBatchId = taskBatchId;
    }

    public BigInteger getParentId() {
        return parentId;
    }

    public void setParentId(BigInteger parentId) {
        this.parentId = parentId;
    }

    public JobTaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(JobTaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }
}
