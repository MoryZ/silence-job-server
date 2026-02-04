package com.old.silence.job.server.dto;



import java.math.BigInteger;
import java.util.Set;



public class ExportJobVO {

    private Set<BigInteger> jobIds;
    private String groupName;
    private String jobName;
    private Boolean jobStatus;

    public Set<BigInteger> getJobIds() {
        return jobIds;
    }

    public void setJobIds(Set<BigInteger> jobIds) {
        this.jobIds = jobIds;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Boolean getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(Boolean jobStatus) {
        this.jobStatus = jobStatus;
    }
}
