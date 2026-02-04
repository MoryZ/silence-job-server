package com.old.silence.job.server.dto;



import java.math.BigInteger;


public class JobTaskConfig {

    /**
     * 任务ID
     */
    private BigInteger jobId;

    /**
     * 任务名称
     */
    private String jobName;

    public BigInteger getJobId() {
        return jobId;
    }

    public void setJobId(BigInteger jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
}
