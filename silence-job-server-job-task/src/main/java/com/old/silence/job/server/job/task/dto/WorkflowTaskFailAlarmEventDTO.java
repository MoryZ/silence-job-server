package com.old.silence.job.server.job.task.dto;


import com.old.silence.job.common.enums.JobNotifyScene;

import java.math.BigInteger;


public class WorkflowTaskFailAlarmEventDTO {

    /**
     * 工作流任务批次id
     */
    private BigInteger workflowTaskBatchId;

    /**
     * 通知场景
     */
    private JobNotifyScene notifyScene;

    /**
     * 失败原因
     */
    private String reason;

    public BigInteger getWorkflowTaskBatchId() {
        return workflowTaskBatchId;
    }

    public void setWorkflowTaskBatchId(BigInteger workflowTaskBatchId) {
        this.workflowTaskBatchId = workflowTaskBatchId;
    }

    public JobNotifyScene getNotifyScene() {
        return notifyScene;
    }

    public void setNotifyScene(JobNotifyScene notifyScene) {
        this.notifyScene = notifyScene;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
