package com.old.silence.job.server.job.task.support.generator.batch;


import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.common.enums.JobTaskExecutorScene;

import java.math.BigInteger;


public class WorkflowTaskBatchGeneratorContext {

    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 工作流id
     */
    private BigInteger workflowId;

    /**
     * 下次触发时间
     */
    private BigInteger nextTriggerAt;

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
     * 流程信息
     */
    private String flowInfo;

    /**
     * 工作流上下文
     */
    private String wfContext;

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

    public BigInteger getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(BigInteger workflowId) {
        this.workflowId = workflowId;
    }

    public BigInteger getNextTriggerAt() {
        return nextTriggerAt;
    }

    public void setNextTriggerAt(BigInteger nextTriggerAt) {
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

    public String getFlowInfo() {
        return flowInfo;
    }

    public void setFlowInfo(String flowInfo) {
        this.flowInfo = flowInfo;
    }

    public String getWfContext() {
        return wfContext;
    }

    public void setWfContext(String wfContext) {
        this.wfContext = wfContext;
    }
}
