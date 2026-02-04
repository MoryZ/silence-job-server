package com.old.silence.job.server.job.task.dto;


import com.old.silence.job.common.enums.JobTaskExecutorScene;

import java.math.BigInteger;

public class TaskExecuteDTO {

    private BigInteger jobId;
    private BigInteger taskBatchId;
    /**
     * 工作流任务批次id
     */
    private BigInteger workflowTaskBatchId;

    private BigInteger workflowNodeId;
    /**
     * 执行策略 1、auto 2、manual 3、workflow
     */
    private JobTaskExecutorScene taskExecutorScene;

    /**
     * 临时任务参数
     */
    private String tmpArgsStr;

    public TaskExecuteDTO() {
    }

    public TaskExecuteDTO(BigInteger jobId, BigInteger taskBatchId, BigInteger workflowTaskBatchId, BigInteger workflowNodeId, JobTaskExecutorScene taskExecutorScene) {
        this.jobId = jobId;
        this.taskBatchId = taskBatchId;
        this.workflowTaskBatchId = workflowTaskBatchId;
        this.workflowNodeId = workflowNodeId;
        this.taskExecutorScene = taskExecutorScene;
    }

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

    public JobTaskExecutorScene getTaskExecutorScene() {
        return taskExecutorScene;
    }

    public void setTaskExecutorScene(JobTaskExecutorScene taskExecutorScene) {
        this.taskExecutorScene = taskExecutorScene;
    }

    public String getTmpArgsStr() {
        return tmpArgsStr;
    }

    public void setTmpArgsStr(String tmpArgsStr) {
        this.tmpArgsStr = tmpArgsStr;
    }
}
