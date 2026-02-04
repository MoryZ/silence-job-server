package com.old.silence.job.server.vo;



import com.old.silence.job.common.enums.ExecutorType;
import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.dto.CallbackConfig;
import com.old.silence.job.server.dto.DecisionConfig;

import java.math.BigInteger;
import java.time.Instant;



public class JobBatchResponseVO {

    private BigInteger id;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 名称
     */
    private String jobName;

    /**
     * 任务类型
     */
    private JobTaskType taskType;

    /**
     * 工作流节点名称
     */
    private String nodeName;

    /**
     * 任务信息id
     */
    private BigInteger jobId;

    /**
     * 任务状态
     */
    private JobTaskBatchStatus taskBatchStatus;

    /**
     * 创建时间
     */
    private Instant createdDate;

    /**
     * 更新时间
     */
    private Instant updatedDate;

    /**
     * 任务执行时间
     */
    private Instant executionAt;
    /**
     * 操作原因
     */
    private JobOperationReason operationReason;

    /**
     * 执行器类型 1、Java
     */
    private ExecutorType executorType;

    /**
     * 执行器名称
     */
    private String executorInfo;

    /**
     * 工作流的回调节点信息
     */
    private CallbackConfig callback;

    /**
     * 工作流的决策节点信息
     */
    private DecisionConfig decision;

    /**
     * 工作流批次id
     */
    private BigInteger workflowTaskBatchId;

    /**
     * 工作流节点id
     */
    private BigInteger workflowNodeId;

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

    public JobTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(JobTaskType taskType) {
        this.taskType = taskType;
    }

    public ExecutorType getExecutorType() {
        return executorType;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public BigInteger getJobId() {
        return jobId;
    }

    public void setJobId(BigInteger jobId) {
        this.jobId = jobId;
    }

    public JobTaskBatchStatus getTaskBatchStatus() {
        return taskBatchStatus;
    }

    public void setTaskBatchStatus(JobTaskBatchStatus taskBatchStatus) {
        this.taskBatchStatus = taskBatchStatus;
    }

    public void setExecutorType(ExecutorType executorType) {
        this.executorType = executorType;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Instant getExecutionAt() {
        return executionAt;
    }

    public void setExecutionAt(Instant executionAt) {
        this.executionAt = executionAt;
    }

    public JobOperationReason getOperationReason() {
        return operationReason;
    }

    public void setOperationReason(JobOperationReason operationReason) {
        this.operationReason = operationReason;
    }


    public String getExecutorInfo() {
        return executorInfo;
    }

    public void setExecutorInfo(String executorInfo) {
        this.executorInfo = executorInfo;
    }

    public CallbackConfig getCallback() {
        return callback;
    }

    public void setCallback(CallbackConfig callback) {
        this.callback = callback;
    }

    public DecisionConfig getDecision() {
        return decision;
    }

    public void setDecision(DecisionConfig decision) {
        this.decision = decision;
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
}
