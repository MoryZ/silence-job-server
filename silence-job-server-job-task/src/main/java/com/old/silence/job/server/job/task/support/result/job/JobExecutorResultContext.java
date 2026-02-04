package com.old.silence.job.server.job.task.support.result.job;

import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.job.task.dto.BaseDTO;

import java.math.BigInteger;
import java.util.List;


public class JobExecutorResultContext extends BaseDTO {

    private BigInteger workflowNodeId;
    private BigInteger workflowTaskBatchId;
    private JobOperationReason jobOperationReason;
    private boolean isRetry;
    private List<JobTask> jobTaskList;

    /**
     * 是否开启创建Reduce任务
     */
    private boolean createReduceTask;

    /**
     * 是否更新批次完成
     */
    private boolean taskBatchComplete;

    /**
     * 原因
     */
    private String message;

    public BigInteger getWorkflowNodeId() {
        return workflowNodeId;
    }

    public void setWorkflowNodeId(BigInteger workflowNodeId) {
        this.workflowNodeId = workflowNodeId;
    }

    public BigInteger getWorkflowTaskBatchId() {
        return workflowTaskBatchId;
    }

    public void setWorkflowTaskBatchId(BigInteger workflowTaskBatchId) {
        this.workflowTaskBatchId = workflowTaskBatchId;
    }

    public JobOperationReason getJobOperationReason() {
        return jobOperationReason;
    }

    public void setJobOperationReason(JobOperationReason jobOperationReason) {
        this.jobOperationReason = jobOperationReason;
    }

    public boolean isRetry() {
        return isRetry;
    }

    public void setRetry(boolean retry) {
        isRetry = retry;
    }

    public List<JobTask> getJobTaskList() {
        return jobTaskList;
    }

    public void setJobTaskList(List<JobTask> jobTaskList) {
        this.jobTaskList = jobTaskList;
    }

    public boolean isCreateReduceTask() {
        return createReduceTask;
    }

    public void setCreateReduceTask(boolean createReduceTask) {
        this.createReduceTask = createReduceTask;
    }

    public boolean isTaskBatchComplete() {
        return taskBatchComplete;
    }

    public void setTaskBatchComplete(boolean taskBatchComplete) {
        this.taskBatchComplete = taskBatchComplete;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
