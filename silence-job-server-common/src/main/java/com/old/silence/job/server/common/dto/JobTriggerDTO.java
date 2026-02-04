package com.old.silence.job.server.common.dto;

import java.math.BigInteger;
import jakarta.validation.constraints.NotNull;


public class JobTriggerDTO {

    @NotNull(message = "jobId 不能为空")
    private BigInteger jobId;

    /**
     * 临时任务参数
     */
    private String tmpArgsStr;

    public BigInteger getJobId() {
        return jobId;
    }

    public void setJobId(BigInteger jobId) {
        this.jobId = jobId;
    }

    public String getTmpArgsStr() {
        return tmpArgsStr;
    }

    public void setTmpArgsStr(String tmpArgsStr) {
        this.tmpArgsStr = tmpArgsStr;
    }
}
