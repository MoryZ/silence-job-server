package com.old.silence.job.server.job.task.dto;


import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskStatus;
import com.old.silence.job.common.enums.JobTaskType;

import java.math.BigInteger;

public class JobExecutorResultDTO {

    private BigInteger jobId;

    private BigInteger taskBatchId;

    /**
     * 工作流任务批次id
     */
    private BigInteger workflowTaskBatchId;

    private BigInteger workflowNodeId;

    private BigInteger taskId;

    /**
     * 命名空间
     */
    private String namespaceId;

    private String groupName;

    private JobTaskStatus taskStatus;

    private String message;

    private JobTaskType taskType;

    private Object result;

    private JobOperationReason jobOperationReason;

    private Boolean isLeaf;

    private String wfContext;

    private Boolean retryStatus = Boolean.FALSE;

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

    public BigInteger getWorkflowTaskBatchId() {
        return workflowTaskBatchId;
    }

    public void setWorkflowTaskBatchId(BigInteger workflowTaskBatchId) {
        this.workflowTaskBatchId = workflowTaskBatchId;
    }

    public BigInteger getWorkflowNodeId() {
        return workflowNodeId;
    }

    public void setWorkflowNodeId(BigInteger workflowNodeId) {
        this.workflowNodeId = workflowNodeId;
    }

    public BigInteger getTaskId() {
        return taskId;
    }

    public void setTaskId(BigInteger taskId) {
        this.taskId = taskId;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public JobTaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(JobTaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JobTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(JobTaskType taskType) {
        this.taskType = taskType;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public JobOperationReason getJobOperationReason() {
        return jobOperationReason;
    }

    public void setJobOperationReason(JobOperationReason jobOperationReason) {
        this.jobOperationReason = jobOperationReason;
    }

    public Boolean getIsLeaf() {
        return isLeaf;
    }

    public void setIsLeaf(Boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    public String getWfContext() {
        return wfContext;
    }

    public void setWfContext(String wfContext) {
        this.wfContext = wfContext;
    }

    public Boolean getRetryStatus() {
        return retryStatus;
    }

    public void setRetryStatus(Boolean retryStatus) {
        this.retryStatus = retryStatus;
    }
}
