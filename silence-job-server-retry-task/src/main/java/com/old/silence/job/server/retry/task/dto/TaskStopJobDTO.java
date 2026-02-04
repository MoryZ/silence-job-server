package com.old.silence.job.server.retry.task.dto;



import com.old.silence.job.common.enums.RetryOperationReason;




public class TaskStopJobDTO extends BaseDTO {

    /**
     * 操作原因
     */
    private RetryOperationReason operationReason;

    /**
     * 若是失败补充失败信息
     */
    private String message;

    /**
     * 是否需要变更任务状态
     */
    private boolean needUpdateTaskStatus;

    public RetryOperationReason getOperationReason() {
        return operationReason;
    }

    public void setOperationReason(RetryOperationReason operationReason) {
        this.operationReason = operationReason;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isNeedUpdateTaskStatus() {
        return needUpdateTaskStatus;
    }

    public void setNeedUpdateTaskStatus(boolean needUpdateTaskStatus) {
        this.needUpdateTaskStatus = needUpdateTaskStatus;
    }
}
