package com.old.silence.job.server.common.dto;


import com.old.silence.job.common.enums.JobOperationReason;

import java.math.BigInteger;


public class JobAlarmInfo extends AlarmInfo {

    private BigInteger id;
    /**
     * 名称
     */
    private String jobName;

    /**
     * 任务信息id
     */
    private BigInteger jobId;
    /**
     * 执行器名称
     */
    private String executorInfo;

    /**
     * 执行参数
     */
    private String argsStr;

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

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public BigInteger getJobId() {
        return jobId;
    }

    public void setJobId(BigInteger jobId) {
        this.jobId = jobId;
    }

    public String getExecutorInfo() {
        return executorInfo;
    }

    public void setExecutorInfo(String executorInfo) {
        this.executorInfo = executorInfo;
    }

    public String getArgsStr() {
        return argsStr;
    }

    public void setArgsStr(String argsStr) {
        this.argsStr = argsStr;
    }

    public JobOperationReason getOperationReason() {
        return operationReason;
    }

    public void setOperationReason(JobOperationReason operationReason) {
        this.operationReason = operationReason;
    }
}
