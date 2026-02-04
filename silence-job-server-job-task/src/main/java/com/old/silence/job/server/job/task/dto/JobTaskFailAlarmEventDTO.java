package com.old.silence.job.server.job.task.dto;


import com.old.silence.job.common.enums.JobNotifyScene;

import java.math.BigInteger;

public class JobTaskFailAlarmEventDTO {

    /**
     * 任务批次id
     */
    private BigInteger jobTaskBatchId;

    /**
     * 通知场景
     */
    private JobNotifyScene notifyScene;

    /**
     * 原因
     */
    private String reason;

    public BigInteger getJobTaskBatchId() {
        return jobTaskBatchId;
    }

    public void setJobTaskBatchId(BigInteger jobTaskBatchId) {
        this.jobTaskBatchId = jobTaskBatchId;
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
