package com.old.silence.job.server.dto;



import java.math.BigInteger;
import java.util.List;


public class JobBatchQueryDO {

    private List<String> groupNames;
    private Integer taskBatchStatus;
    private String jobName;
    private BigInteger jobId;
    /**
     * 命名空间id
     */
    private String namespaceId;

    public List<String> getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(List<String> groupNames) {
        this.groupNames = groupNames;
    }

    public Integer getTaskBatchStatus() {
        return taskBatchStatus;
    }

    public void setTaskBatchStatus(Integer taskBatchStatus) {
        this.taskBatchStatus = taskBatchStatus;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public BigInteger getJobId() {
        return jobId;
    }

    public void setJobId(BigInteger jobId) {
        this.jobId = jobId;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
}
