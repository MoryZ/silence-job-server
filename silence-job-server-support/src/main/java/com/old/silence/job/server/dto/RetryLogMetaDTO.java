package com.old.silence.job.server.common.dto;

import com.alibaba.fastjson2.JSON;
import com.old.silence.job.log.enums.LogTypeEnum;

import java.math.BigInteger;




public class RetryLogMetaDTO extends LogMetaDTO {

    public RetryLogMetaDTO() {
        setLogType(LogTypeEnum.RETRY);
    }

    /**
     * 重试任务id
     */
    private BigInteger retryTaskId;

    /**
     * 重试信息Id
     */
    private BigInteger retryId;

    public BigInteger getRetryTaskId() {
        return retryTaskId;
    }

    public void setRetryTaskId(BigInteger retryTaskId) {
        this.retryTaskId = retryTaskId;
    }

    public BigInteger getRetryId() {
        return retryId;
    }

    public void setRetryId(BigInteger retryId) {
        this.retryId = retryId;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
