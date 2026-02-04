package com.old.silence.job.server.job.task.dto;


import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.common.enums.JobTaskExecutorScene;

import java.math.BigInteger;

public class WorkflowTaskPrepareDTO {

    private BigInteger workflowTaskBatchId;

    private BigInteger workflowId;

    /**
     * 执行策略 1、auto 2、manual 3、workflow
     */
    private JobTaskExecutorScene taskExecutorScene;

    /**
     * 阻塞策略 1、丢弃 2、覆盖 3、并行
     */
    private JobBlockStrategy blockStrategy;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 命名空间id
     */
    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 触发间隔
     */
    private String triggerInterval;

    /**
     * 执行超时时间
     */
    private Integer executorTimeout;

    /**
     * 工作流状态 0、关闭、1、开启
     */
    private Boolean workflowStatus;

    /**
     * 流程信息
     */
    private String flowInfo;

    /**
     * 下次触发时间
     */
    private long nextTriggerAt;

    /**
     * 任务执行时间
     */
    private Long executionAt;

    /**
     * 仅做超时检测
     */
    private boolean onlyTimeoutCheck;

    /**
     * 工作流上下文
     */
    private String wfContext;

    public BigInteger getWorkflowTaskBatchId() {
        return workflowTaskBatchId;
    }

    public void setWorkflowTaskBatchId(BigInteger workflowTaskBatchId) {
        this.workflowTaskBatchId = workflowTaskBatchId;
    }

    public BigInteger getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(BigInteger workflowId) {
        this.workflowId = workflowId;
    }

    public JobTaskExecutorScene getTaskExecutorScene() {
        return taskExecutorScene;
    }

    public void setTaskExecutorScene(JobTaskExecutorScene taskExecutorScene) {
        this.taskExecutorScene = taskExecutorScene;
    }

    public JobBlockStrategy getBlockStrategy() {
        return blockStrategy;
    }

    public void setBlockStrategy(JobBlockStrategy blockStrategy) {
        this.blockStrategy = blockStrategy;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
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

    public String getTriggerInterval() {
        return triggerInterval;
    }

    public void setTriggerInterval(String triggerInterval) {
        this.triggerInterval = triggerInterval;
    }

    public Integer getExecutorTimeout() {
        return executorTimeout;
    }

    public void setExecutorTimeout(Integer executorTimeout) {
        this.executorTimeout = executorTimeout;
    }

    public Boolean getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(Boolean workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public String getFlowInfo() {
        return flowInfo;
    }

    public void setFlowInfo(String flowInfo) {
        this.flowInfo = flowInfo;
    }

    public long getNextTriggerAt() {
        return nextTriggerAt;
    }

    public void setNextTriggerAt(long nextTriggerAt) {
        this.nextTriggerAt = nextTriggerAt;
    }

    public Long getExecutionAt() {
        return executionAt;
    }

    public void setExecutionAt(Long executionAt) {
        this.executionAt = executionAt;
    }

    public boolean isOnlyTimeoutCheck() {
        return onlyTimeoutCheck;
    }

    public void setOnlyTimeoutCheck(boolean onlyTimeoutCheck) {
        this.onlyTimeoutCheck = onlyTimeoutCheck;
    }

    public String getWfContext() {
        return wfContext;
    }

    public void setWfContext(String wfContext) {
        this.wfContext = wfContext;
    }
}
