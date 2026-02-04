package com.old.silence.job.server.job.task.dto;


import java.math.BigInteger;

public class JobLogDTO {

    /**
     * 命名空间
     */
    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 任务信息id
     */
    private BigInteger jobId;

    /**
     * 任务实例id
     */
    private BigInteger taskBatchId;

    /**
     * 调度任务id
     */
    private BigInteger taskId;


    /**
     * 调度信息
     */
    private String message;

    /**
     * 真实上报时间
     */
    private Long realTime;

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

    public BigInteger getTaskId() {
        return taskId;
    }

    public void setTaskId(BigInteger taskId) {
        this.taskId = taskId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getRealTime() {
        return realTime;
    }

    public void setRealTime(Long realTime) {
        this.realTime = realTime;
    }
}
