package com.old.silence.job.server.dto;

import java.math.BigInteger;



public class JobLogQuery {
    private BigInteger startId;
    private BigInteger jobId;
    private BigInteger taskBatchId;
    private BigInteger taskId;
    private Integer fromIndex;

    public BigInteger getStartId() {
        return startId;
    }

    public void setStartId(BigInteger startId) {
        this.startId = startId;
    }

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

    public BigInteger getTaskId() {
        return taskId;
    }

    public void setTaskId(BigInteger taskId) {
        this.taskId = taskId;
    }

    public Integer getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(Integer fromIndex) {
        this.fromIndex = fromIndex;
    }
}
