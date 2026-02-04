package com.old.silence.job.server.job.task.support.generator.batch;


import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.common.enums.JobTaskExecutorScene;

import java.math.BigInteger;


public class JobTaskBatchGeneratorContext {


    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 任务id
     */
    private BigInteger jobId;

    /**
     * 下次触发时间
     */
    private Long nextTriggerAt;

    /**
     * 操作原因
     */
    private JobOperationReason operationReason;

    /**
     * 任务批次状态
     */
    private JobTaskBatchStatus taskBatchStatus;

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

    /**
     * 工作流上下文
     */
    private String wfContext;

    /**
     * 临时任务参数
     */
    private String tmpArgsStr;

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

    public BigInteger getJobId() {
        return jobId;
    }

    public void setJobId(BigInteger jobId) {
        this.jobId = jobId;
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

    public JobTaskBatchStatus getTaskBatchStatus() {
        return taskBatchStatus;
    }

    public void setTaskBatchStatus(JobTaskBatchStatus taskBatchStatus) {
        this.taskBatchStatus = taskBatchStatus;
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

    public String getWfContext() {
        return wfContext;
    }

    public void setWfContext(String wfContext) {
        this.wfContext = wfContext;
    }

    public String getTmpArgsStr() {
        return tmpArgsStr;
    }

    public void setTmpArgsStr(String tmpArgsStr) {
        this.tmpArgsStr = tmpArgsStr;
    }
}
