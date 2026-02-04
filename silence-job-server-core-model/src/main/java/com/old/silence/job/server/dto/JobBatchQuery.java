package com.old.silence.job.server.dto;


import com.old.silence.data.commons.annotation.RelationalQueryProperty;
import com.old.silence.data.commons.converter.Part;
import com.old.silence.job.common.enums.JobTaskBatchStatus;

import java.math.BigInteger;
import java.util.List;


public class JobBatchQuery {
    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private BigInteger jobId;
    //@RelationalQueryProperty(name = "", type = Part.Type.STARTING_WITH)
    private String jobName;
    @RelationalQueryProperty(name = "taskBatchStatus", type = Part.Type.IN)
    private List<JobTaskBatchStatus> jobTaskBatchStatuses;
    @RelationalQueryProperty(type = Part.Type.STARTING_WITH)
    private String groupName;

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

    public List<JobTaskBatchStatus> getJobTaskBatchStatuses() {
        return jobTaskBatchStatuses;
    }

    public void setJobTaskBatchStatuses(List<JobTaskBatchStatus> jobTaskBatchStatuses) {
        this.jobTaskBatchStatuses = jobTaskBatchStatuses;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
