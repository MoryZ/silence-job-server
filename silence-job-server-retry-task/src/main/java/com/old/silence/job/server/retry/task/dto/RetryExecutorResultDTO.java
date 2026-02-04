package com.old.silence.job.server.retry.task.dto;



import com.old.silence.job.common.enums.RetryOperationReason;
import com.old.silence.job.common.enums.RetryTaskStatus;

public class RetryExecutorResultDTO extends BaseDTO  {

    private RetryOperationReason operationReason;
    private boolean incrementRetryCount;
    private String resultJson;
    private String exceptionMsg;
    private RetryTaskStatus taskStatus;

    public RetryOperationReason getOperationReason() {
        return operationReason;
    }

    public void setOperationReason(RetryOperationReason operationReason) {
        this.operationReason = operationReason;
    }

    public boolean isIncrementRetryCount() {
        return incrementRetryCount;
    }

    public void setIncrementRetryCount(boolean incrementRetryCount) {
        this.incrementRetryCount = incrementRetryCount;
    }

    public String getResultJson() {
        return resultJson;
    }

    public void setResultJson(String resultJson) {
        this.resultJson = resultJson;
    }

    public String getExceptionMsg() {
        return exceptionMsg;
    }

    public void setExceptionMsg(String exceptionMsg) {
        this.exceptionMsg = exceptionMsg;
    }

    public RetryTaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(RetryTaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }
}
