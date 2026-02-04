package com.old.silence.job.server.retry.task.support.result;

import com.old.silence.job.common.enums.RetryOperationReason;
import com.old.silence.job.common.enums.RetryTaskStatus;
import com.old.silence.job.server.retry.task.dto.BaseDTO;



/**
 * <p>
 *
 * </p>
 *
 */


public class RetryResultContext extends BaseDTO {

//    /**
//     * 客户端返回的结果
//     * @see RetryResultStatusEnum
//     */
//    private Integer resultStatus;

    /**
     * 重试任务状态
     * @see RetryTaskStatus
     */
    private RetryTaskStatus taskStatus;
    private RetryOperationReason operationReason;

    private boolean incrementRetryCount;
    private String resultJson;
    private String idempotentId;
    private String exceptionMsg;

    public RetryTaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(RetryTaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

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

    public String getIdempotentId() {
        return idempotentId;
    }

    public void setIdempotentId(String idempotentId) {
        this.idempotentId = idempotentId;
    }

    public String getExceptionMsg() {
        return exceptionMsg;
    }

    public void setExceptionMsg(String exceptionMsg) {
        this.exceptionMsg = exceptionMsg;
    }
}
