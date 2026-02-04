package com.old.silence.job.server.job.task.support.callback;

import com.old.silence.job.common.client.dto.ExecuteResult;
import com.old.silence.job.common.enums.JobTaskStatus;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.domain.model.JobTask;

import java.math.BigInteger;


public class ClientCallbackContext {

    private BigInteger jobId;

    /**
     * 命名空间
     */
    private String namespaceId;

    private BigInteger taskBatchId;

    /**
     * 工作流任务批次id
     */
    private BigInteger workflowTaskBatchId;

    private BigInteger workflowNodeId;

    private BigInteger taskId;

    private String groupName;

    private JobTaskStatus taskStatus;

    private ExecuteResult executeResult;

    private String clientInfo;

    private Job job;

    private JobTask jobTask;

    private Integer retryScene;

    @Deprecated
    private boolean isRetry;

    /**
     * 是否是重试流量
     */
    private Boolean retryStatus;
    /**
     * 工作流上下文
     */
    private String wfContext;

    // 兼容isRetry/retryStatus并存
    @Deprecated
    public Boolean getRetryStatus() {
        return Boolean.TRUE.equals(retryStatus) || isRetry;
    }

    // 兼容isRetry/retryStatus并存
    @Deprecated
    public void setRetryStatus(boolean value) {
        this.retryStatus = Boolean.valueOf(value);
        isRetry = value;
    }

    public void setRetryStatus(Boolean retryStatus) {
        this.retryStatus = retryStatus;
    }

    public BigInteger getJobId() {
        return jobId;
    }

    public void setJobId(BigInteger jobId) {
        this.jobId = jobId;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public BigInteger getTaskBatchId() {
        return taskBatchId;
    }

    public void setTaskBatchId(BigInteger taskBatchId) {
        this.taskBatchId = taskBatchId;
    }

    public BigInteger getWorkflowTaskBatchId() {
        return workflowTaskBatchId;
    }

    public void setWorkflowTaskBatchId(BigInteger workflowTaskBatchId) {
        this.workflowTaskBatchId = workflowTaskBatchId;
    }

    public BigInteger getWorkflowNodeId() {
        return workflowNodeId;
    }

    public void setWorkflowNodeId(BigInteger workflowNodeId) {
        this.workflowNodeId = workflowNodeId;
    }

    public BigInteger getTaskId() {
        return taskId;
    }

    public void setTaskId(BigInteger taskId) {
        this.taskId = taskId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public JobTaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(JobTaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public ExecuteResult getExecuteResult() {
        return executeResult;
    }

    public void setExecuteResult(ExecuteResult executeResult) {
        this.executeResult = executeResult;
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(String clientInfo) {
        this.clientInfo = clientInfo;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public JobTask getJobTask() {
        return jobTask;
    }

    public void setJobTask(JobTask jobTask) {
        this.jobTask = jobTask;
    }

    public Integer getRetryScene() {
        return retryScene;
    }

    public void setRetryScene(Integer retryScene) {
        this.retryScene = retryScene;
    }

    public boolean isRetry() {
        return isRetry;
    }

    public void setRetry(boolean retry) {
        isRetry = retry;
    }

    public String getWfContext() {
        return wfContext;
    }

    public void setWfContext(String wfContext) {
        this.wfContext = wfContext;
    }
}
