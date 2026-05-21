package com.old.silence.job.server.domain.dto;

import java.math.BigInteger;

/**
 * 日志查询 DTO
 */
public class JobLogQueryDTO {

    /**
     * 任务批次ID
     */
    private BigInteger taskBatchId;

    /**
     * 任务ID
     */
    private BigInteger taskId;

    /**
     * 会话ID
     */
    private String sid;

    /**
     * 起始时间戳
     */
    private Long startRealTime;

    public BigInteger getTaskBatchId() {
        return taskBatchId;
    }

    public void setTaskBatchId(BigInteger taskBatchId) {
        this.taskBatchId = taskBatchId;
    }

    public BigInteger getTaskId() {
        return taskId;
    }

    public void setTaskId(BigInteger taskId) {
        this.taskId = taskId;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public Long getStartRealTime() {
        return startRealTime;
    }

    public void setStartRealTime(Long startRealTime) {
        this.startRealTime = startRealTime;
    }
}
