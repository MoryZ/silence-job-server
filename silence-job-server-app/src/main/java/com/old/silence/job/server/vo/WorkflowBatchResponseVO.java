package com.old.silence.job.server.vo;



import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskBatchStatus;

import java.math.BigInteger;
import java.time.Instant;



public class WorkflowBatchResponseVO {

    private BigInteger id;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 工作流任务id
     */
    private BigInteger workflowId;

    /**
     * 工作流任务名称
     */
    private String workflowName;

    /**
     * 任务批次状态 0、失败 1、成功
     */
    private JobTaskBatchStatus taskBatchStatus;

    /**
     * 操作原因
     */
    private JobOperationReason operationReason;

    /**
     * 任务执行时间
     */
    private Instant executionAt;

    /**
     * 创建时间
     */
    private Instant createdDate;

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

    public BigInteger getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(BigInteger workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public JobTaskBatchStatus getTaskBatchStatus() {
        return taskBatchStatus;
    }

    public void setTaskBatchStatus(JobTaskBatchStatus taskBatchStatus) {
        this.taskBatchStatus = taskBatchStatus;
    }

    public JobOperationReason getOperationReason() {
        return operationReason;
    }

    public void setOperationReason(JobOperationReason operationReason) {
        this.operationReason = operationReason;
    }

    public Instant getExecutionAt() {
        return executionAt;
    }

    public void setExecutionAt(Instant executionAt) {
        this.executionAt = executionAt;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }
}
