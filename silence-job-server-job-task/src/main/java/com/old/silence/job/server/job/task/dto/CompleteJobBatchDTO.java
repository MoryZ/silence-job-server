package com.old.silence.job.server.job.task.dto;


import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskType;

import java.math.BigInteger;

public class CompleteJobBatchDTO extends BaseDTO {

    private BigInteger jobId;
    private BigInteger workflowNodeId;
    private BigInteger workflowTaskBatchId;
    private BigInteger taskBatchId;
    private JobOperationReason jobOperationReason;
    private Object result;
    private String message;
    private JobTaskType taskType;
    private Boolean retryStatus;

    @Override
    public BigInteger getJobId() {
        return jobId;
    }

    @Override
    public void setJobId(BigInteger jobId) {
        this.jobId = jobId;
    }

    public BigInteger getWorkflowNodeId() {
        return workflowNodeId;
    }

    public void setWorkflowNodeId(BigInteger workflowNodeId) {
        this.workflowNodeId = workflowNodeId;
    }

    public BigInteger getWorkflowTaskBatchId() {
        return workflowTaskBatchId;
    }

    public void setWorkflowTaskBatchId(BigInteger workflowTaskBatchId) {
        this.workflowTaskBatchId = workflowTaskBatchId;
    }

    @Override
    public BigInteger getTaskBatchId() {
        return taskBatchId;
    }

    @Override
    public void setTaskBatchId(BigInteger taskBatchId) {
        this.taskBatchId = taskBatchId;
    }

    public JobOperationReason getJobOperationReason() {
        return jobOperationReason;
    }

    public void setJobOperationReason(JobOperationReason jobOperationReason) {
        this.jobOperationReason = jobOperationReason;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public JobTaskType getTaskType() {
        return taskType;
    }

    @Override
    public void setTaskType(JobTaskType taskType) {
        this.taskType = taskType;
    }

    public Boolean getRetryStatus() {
        return retryStatus;
    }

    public void setRetryStatus(Boolean retryStatus) {
        this.retryStatus = retryStatus;
    }
}
