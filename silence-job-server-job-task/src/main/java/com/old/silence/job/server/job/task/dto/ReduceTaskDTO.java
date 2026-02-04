package com.old.silence.job.server.job.task.dto;


import com.old.silence.job.common.enums.MapReduceStage;

import java.math.BigInteger;

public class ReduceTaskDTO {

    private BigInteger workflowNodeId;
    private BigInteger workflowTaskBatchId;
    private BigInteger taskBatchId;
    private BigInteger jobId;
    private MapReduceStage mrStage;
    private String wfContext;

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

    public BigInteger getTaskBatchId() {
        return taskBatchId;
    }

    public void setTaskBatchId(BigInteger taskBatchId) {
        this.taskBatchId = taskBatchId;
    }

    public BigInteger getJobId() {
        return jobId;
    }

    public void setJobId(BigInteger jobId) {
        this.jobId = jobId;
    }

    public MapReduceStage getMrStage() {
        return mrStage;
    }

    public void setMrStage(MapReduceStage mrStage) {
        this.mrStage = mrStage;
    }

    public String getWfContext() {
        return wfContext;
    }

    public void setWfContext(String wfContext) {
        this.wfContext = wfContext;
    }
}
