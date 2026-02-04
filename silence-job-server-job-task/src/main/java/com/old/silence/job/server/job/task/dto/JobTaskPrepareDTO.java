package com.old.silence.job.server.job.task.dto;


import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.common.enums.JobTaskExecutorScene;
import com.old.silence.job.common.enums.JobTaskType;

import java.math.BigInteger;

public class JobTaskPrepareDTO {

    private BigInteger jobId;


    /**
     * 组名称
     */
    private String groupName;

    /**
     * 下次触发时间
     */
    private long nextTriggerAt;

    /**
     * 阻塞策略 1、丢弃 2、覆盖 3、并行
     */
    private JobBlockStrategy blockStrategy;

    /**
     * 任务类型
     */
    private JobTaskType taskType;

    /**
     * 任务执行超时时间，单位秒
     */
    private Integer executorTimeout;

    private BigInteger taskBatchId;

    private String clientId;

    /**
     * 任务执行时间
     */
    private Long executionAt;

    private boolean onlyTimeoutCheck;

    /**
     * 执行策略 1、auto_job 2、manual_job 3、auto_workflow 4、manual_workflow
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
     * 临时任务参数
     */
    private String tmpArgsStr;

    public BigInteger getJobId() {
        return jobId;
    }

    public void setJobId(BigInteger jobId) {
        this.jobId = jobId;
    }


    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public long getNextTriggerAt() {
        return nextTriggerAt;
    }

    public void setNextTriggerAt(long nextTriggerAt) {
        this.nextTriggerAt = nextTriggerAt;
    }

    public JobBlockStrategy getBlockStrategy() {
        return blockStrategy;
    }

    public void setBlockStrategy(JobBlockStrategy blockStrategy) {
        this.blockStrategy = blockStrategy;
    }

    public JobTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(JobTaskType taskType) {
        this.taskType = taskType;
    }

    public Integer getExecutorTimeout() {
        return executorTimeout;
    }

    public void setExecutorTimeout(Integer executorTimeout) {
        this.executorTimeout = executorTimeout;
    }

    public BigInteger getTaskBatchId() {
        return taskBatchId;
    }

    public void setTaskBatchId(BigInteger taskBatchId) {
        this.taskBatchId = taskBatchId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
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

    public String getTmpArgsStr() {
        return tmpArgsStr;
    }

    public void setTmpArgsStr(String tmpArgsStr) {
        this.tmpArgsStr = tmpArgsStr;
    }
}
