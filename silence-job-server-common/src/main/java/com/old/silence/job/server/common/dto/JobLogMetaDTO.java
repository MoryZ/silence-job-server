package com.old.silence.job.server.common.dto;

import com.alibaba.fastjson2.JSON;
import com.old.silence.job.log.enums.LogTypeEnum;

import java.math.BigInteger;



public class JobLogMetaDTO extends LogMetaDTO {

    public JobLogMetaDTO() {
        setLogType(LogTypeEnum.JOB);
    }

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

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
