package com.old.silence.job.server.dto;



import java.math.BigInteger;
import java.util.Set;



public class ExportWorkflowVO {

    private Set<BigInteger> workflowIds;

    private String groupName;

    private String workflowName;

    private Boolean workflowStatus;

    public Set<BigInteger> getWorkflowIds() {
        return workflowIds;
    }

    public void setWorkflowIds(Set<BigInteger> workflowIds) {
        this.workflowIds = workflowIds;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public Boolean getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(Boolean workflowStatus) {
        this.workflowStatus = workflowStatus;
    }
}
