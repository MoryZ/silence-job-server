package com.old.silence.job.server.job.task.support.stop;


import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.domain.model.JobTask;

import java.math.BigInteger;
import java.util.List;


public class TaskStopJobContext {

    /**
     * 命名空间
     */
    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 任务id
     */
    private BigInteger jobId;

    /**
     * 任务id
     */
    private BigInteger taskBatchId;

    /**
     * 任务类型
     */
    private JobTaskType taskType;

    /**
     * 是否需要变更任务状态
     */
    private boolean needUpdateTaskStatus;

    private List<JobTask> jobTasks;

    private JobOperationReason jobOperationReason;

    private boolean forceStop;

    /**
     * 工作流任务批次id
     */
    private BigInteger workflowTaskBatchId;

    private BigInteger workflowNodeId;

    protected List<JobTask> getJobTasks() {
        return jobTasks;
    }

    protected void setJobTasks(List<JobTask> jobTasks) {
        this.jobTasks = jobTasks;
    }


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

    public JobTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(JobTaskType taskType) {
        this.taskType = taskType;
    }

    public boolean isNeedUpdateTaskStatus() {
        return needUpdateTaskStatus;
    }

    public void setNeedUpdateTaskStatus(boolean needUpdateTaskStatus) {
        this.needUpdateTaskStatus = needUpdateTaskStatus;
    }

    public JobOperationReason getJobOperationReason() {
        return jobOperationReason;
    }

    public void setJobOperationReason(JobOperationReason jobOperationReason) {
        this.jobOperationReason = jobOperationReason;
    }

    public boolean isForceStop() {
        return forceStop;
    }

    public void setForceStop(boolean forceStop) {
        this.forceStop = forceStop;
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
}
