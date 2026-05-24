package com.old.silence.job.server.dto;


import com.old.silence.data.commons.annotation.RelationalQueryProperty;
import com.old.silence.data.commons.converter.Part;

public class WorkflowQuery {

    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private String groupName;

    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private String workflowName;

    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
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
