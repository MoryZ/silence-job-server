package com.old.silence.job.server.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;


public class WorkflowTriggerVO {

    @NotNull(message = "workflowId 不能为空")
    private BigInteger workflowId;

    /**
     * 临时工作流上下文
     */
    private String tmpWfContext;

    public BigInteger getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(BigInteger workflowId) {
        this.workflowId = workflowId;
    }

    public String getTmpWfContext() {
        return tmpWfContext;
    }

    public void setTmpWfContext(String tmpWfContext) {
        this.tmpWfContext = tmpWfContext;
    }
}
