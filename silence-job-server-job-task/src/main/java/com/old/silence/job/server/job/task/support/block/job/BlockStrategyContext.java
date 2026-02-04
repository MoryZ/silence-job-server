package com.old.silence.job.server.job.task.support.block.job;


import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskExecutorScene;
import com.old.silence.job.common.enums.JobTaskType;

import java.math.BigInteger;

public class BlockStrategyContext {

    private BigInteger jobId;

    private BigInteger taskBatchId;

    private String namespaceId;

    private String groupName;

    /**
     * 任务类型
     */
    private JobTaskType taskType;

    /**
     * 下次触发时间
     */
    private Long nextTriggerAt;

    private JobOperationReason operationReason;

    /**
     * 执行策略 1、auto 2、manual 3、workflow
     */
    private JobTaskExecutorScene taskExecutorScene;

    /**
     * 工作流任务批次id
     */
    private BigInteger workflowTaskBatchId;

    /**
     * 工作流节点id
     */
    private BigInteger workflowNodeId;

    /**
     * 工作流父节点id
     */
    private BigInteger parentWorkflowNodeId;

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

    public JobTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(JobTaskType taskType) {
        this.taskType = taskType;
    }

    public Long getNextTriggerAt() {
        return nextTriggerAt;
    }

    public void setNextTriggerAt(Long nextTriggerAt) {
        this.nextTriggerAt = nextTriggerAt;
    }

    public JobOperationReason getOperationReason() {
        return operationReason;
    }

    public void setOperationReason(JobOperationReason operationReason) {
        this.operationReason = operationReason;
    }

    public JobTaskExecutorScene getTaskExecutorScene() {
        return taskExecutorScene;
    }

    public void setTaskExecutorScene(JobTaskExecutorScene taskExecutorScene) {
        this.taskExecutorScene = taskExecutorScene;
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

    public BigInteger getParentWorkflowNodeId() {
        return parentWorkflowNodeId;
    }

    public void setParentWorkflowNodeId(BigInteger parentWorkflowNodeId) {
        this.parentWorkflowNodeId = parentWorkflowNodeId;
    }
}
