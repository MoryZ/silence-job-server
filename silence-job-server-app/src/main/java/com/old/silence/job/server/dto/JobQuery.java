package com.old.silence.job.server.dto;


import com.old.silence.data.commons.annotation.RelationalQueryProperty;
import com.old.silence.data.commons.converter.Part;

import java.math.BigInteger;

public class JobQuery {

    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private BigInteger id;
    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private String groupName;
    @RelationalQueryProperty(type = Part.Type.STARTING_WITH)
    private String jobName;
    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private Boolean jobStatus;
    @RelationalQueryProperty(type = Part.Type.STARTING_WITH)
    private String ownerId;
    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private String executorInfo;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
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

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getExecutorInfo() {
        return executorInfo;
    }

    public void setExecutorInfo(String executorInfo) {
        this.executorInfo = executorInfo;
    }
}
