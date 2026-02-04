package com.old.silence.job.server.common.dto;




import com.old.silence.job.common.enums.JobOperationReason;

import java.math.BigInteger;




public class WorkflowAlarmInfo extends AlarmInfo {

    private BigInteger id;

    /**
     * 名称
     */
    private String workflowName;

    /**
     * 任务信息id
     */
    private BigInteger workflowId;

    /**
     * 操作原因
     */
    private JobOperationReason operationReason;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public BigInteger getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(BigInteger workflowId) {
        this.workflowId = workflowId;
    }

    public JobOperationReason getOperationReason() {
        return operationReason;
    }

    public void setOperationReason(JobOperationReason operationReason) {
        this.operationReason = operationReason;
    }
}
