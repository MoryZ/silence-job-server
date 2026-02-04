package com.old.silence.job.server.dto;



import java.math.BigInteger;
import java.util.List;

import com.old.silence.job.common.enums.JobTaskBatchStatus;


public class WorkflowBatchQueryDO {

    private List<String> groupNames;
    private JobTaskBatchStatus taskBatchStatus;
    private String workflowName;
    private BigInteger workflowId;
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

    public JobTaskBatchStatus getTaskBatchStatus() {
        return taskBatchStatus;
    }

    public void setTaskBatchStatus(JobTaskBatchStatus taskBatchStatus) {
        this.taskBatchStatus = taskBatchStatus;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public BigInteger getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(BigInteger workflowId) {
        this.workflowId = workflowId;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
}
