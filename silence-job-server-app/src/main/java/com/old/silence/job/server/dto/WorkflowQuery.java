package com.old.silence.job.server.dto;


public class WorkflowQuery {

    private String groupName;

    private String workflowName;

    private Boolean workflowStatus;

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
