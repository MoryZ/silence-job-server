package com.old.silence.job.server.job.task.support.block.workflow;

import com.old.silence.job.server.job.task.support.block.job.BlockStrategyContext;

import java.math.BigInteger;


public class WorkflowBlockStrategyContext extends BlockStrategyContext {

    /**
     * 工作流id
     */
    private BigInteger workflowId;

    /**
     * 工作流任务批次id
     */
    private BigInteger workflowTaskBatchId;

    /**
     * 流程信息
     */
    private String flowInfo;

    public BigInteger getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(BigInteger workflowId) {
        this.workflowId = workflowId;
    }

    @Override
    public BigInteger getWorkflowTaskBatchId() {
        return workflowTaskBatchId;
    }

    @Override
    public void setWorkflowTaskBatchId(BigInteger workflowTaskBatchId) {
        this.workflowTaskBatchId = workflowTaskBatchId;
    }

    public String getFlowInfo() {
        return flowInfo;
    }

    public void setFlowInfo(String flowInfo) {
        this.flowInfo = flowInfo;
    }
}
