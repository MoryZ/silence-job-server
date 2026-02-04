package com.old.silence.job.server.job.task.support.executor.workflow;

import com.old.silence.job.common.enums.FailStrategy;
import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.common.enums.JobTaskExecutorScene;
import com.old.silence.job.common.enums.JobTaskStatus;
import com.old.silence.job.common.enums.WorkflowNodeType;
import com.old.silence.job.server.domain.model.Job;

import java.math.BigInteger;


public class WorkflowExecutorContext {

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
     * 工作流任务批次id
     */
    private BigInteger workflowTaskBatchId;

    /**
     * 工作流节点id
     */
    private BigInteger workflowNodeId;

    /**
     * 工作流父节点id
     */
    private BigInteger parentWorkflowNodeId;

    /**
     * TODO 父节点批次状态
     */
    private JobTaskStatus parentJobTaskStatus;

    /**
     * 父节点批次操作原因状态
     */
    private JobOperationReason parentOperationReason;

    /**
     * 任务属性
     */
    private Job job;

    /**
     * 客户端返回的结果
     */
    private String taskResult;

    /**
     * 失败策略 1、跳过 2、阻塞
     */
    private FailStrategy failStrategy;

    /**
     * 工作流节点状态 0、关闭、1、开启
     */
    private Boolean workflowNodeStatus;

    /**
     * 条件节点的判定结果
     */
    private Object evaluationResult;

    /**
     * 调度任务id
     */
    private BigInteger taskBatchId;

    /**
     * 节点信息
     */
    private String nodeInfo;

    /**
     * 任务批次状态
     */
    private JobTaskBatchStatus taskBatchStatus;

    /**
     * 操作原因
     */
    private JobOperationReason operationReason;

    /**
     * 任务状态
     */
    private JobTaskStatus jobTaskStatus;

    /**
     * 日志信息
     */
    private String logMessage;

    /**
     * 执行策略 1、auto 2、manual 3、workflow
     */
    private JobTaskExecutorScene taskExecutorScene;

    /**
     * 1、任务节点 2、条件节点 3、回调节点
     */
    private WorkflowNodeType nodeType;

    /**
     * 工作流全局上下文
     */
    private String wfContext;


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

    public BigInteger getParentWorkflowNodeId() {
        return parentWorkflowNodeId;
    }

    public void setParentWorkflowNodeId(BigInteger parentWorkflowNodeId) {
        this.parentWorkflowNodeId = parentWorkflowNodeId;
    }

    public JobTaskStatus getParentJobTaskStatus() {
        return parentJobTaskStatus;
    }

    public void setParentJobTaskStatus(JobTaskStatus parentJobTaskStatus) {
        this.parentJobTaskStatus = parentJobTaskStatus;
    }

    public JobOperationReason getParentOperationReason() {
        return parentOperationReason;
    }

    public void setParentOperationReason(JobOperationReason parentOperationReason) {
        this.parentOperationReason = parentOperationReason;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public String getTaskResult() {
        return taskResult;
    }

    public void setTaskResult(String taskResult) {
        this.taskResult = taskResult;
    }

    public FailStrategy getFailStrategy() {
        return failStrategy;
    }

    public void setFailStrategy(FailStrategy failStrategy) {
        this.failStrategy = failStrategy;
    }

    public Boolean getWorkflowNodeStatus() {
        return workflowNodeStatus;
    }

    public void setWorkflowNodeStatus(Boolean workflowNodeStatus) {
        this.workflowNodeStatus = workflowNodeStatus;
    }

    public Object getEvaluationResult() {
        return evaluationResult;
    }

    public void setEvaluationResult(Object evaluationResult) {
        this.evaluationResult = evaluationResult;
    }

    public BigInteger getTaskBatchId() {
        return taskBatchId;
    }

    public void setTaskBatchId(BigInteger taskBatchId) {
        this.taskBatchId = taskBatchId;
    }

    public String getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(String nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    public JobTaskBatchStatus getTaskBatchStatus() {
        return taskBatchStatus;
    }

    public void setTaskBatchStatus(JobTaskBatchStatus taskBatchStatus) {
        this.taskBatchStatus = taskBatchStatus;
    }

    public JobOperationReason getOperationReason() {
        return operationReason;
    }

    public void setOperationReason(JobOperationReason operationReason) {
        this.operationReason = operationReason;
    }

    public JobTaskStatus getJobTaskStatus() {
        return jobTaskStatus;
    }

    public void setJobTaskStatus(JobTaskStatus jobTaskStatus) {
        this.jobTaskStatus = jobTaskStatus;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public JobTaskExecutorScene getTaskExecutorScene() {
        return taskExecutorScene;
    }

    public void setTaskExecutorScene(JobTaskExecutorScene taskExecutorScene) {
        this.taskExecutorScene = taskExecutorScene;
    }

    public WorkflowNodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(WorkflowNodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getWfContext() {
        return wfContext;
    }

    public void setWfContext(String wfContext) {
        this.wfContext = wfContext;
    }
}
