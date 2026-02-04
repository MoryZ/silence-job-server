package com.old.silence.job.server.job.task.support.executor.job;

import com.old.silence.job.common.enums.ExecutorType;
import com.old.silence.job.common.enums.JobArgsType;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.domain.model.JobTask;

import java.math.BigInteger;
import java.util.List;


public class JobExecutorContext {

    /**
     * 命名空间id
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

    private List<JobTask> taskList;

    /**
     * 执行方法参数
     */
    private String argsStr;

    /**
     * 参数类型 text/json
     */
    private JobArgsType argsType;

    private Integer routeKey;

    /**
     * 扩展字段
     */
    private String extAttrs;


    private BigInteger taskId;


    private Integer parallelNum;

    private ExecutorType executorType;

    private String executorInfo;


    /**
     * 最大重试次数
     */
    private Integer maxRetryTimes;

    /**
     * 重试间隔(s)
     */
    private Integer retryInterval;

    private Integer shardingTotal;

    private Integer shardingIndex;

    private Integer executorTimeout;
    /**
     * 工作流任务批次id
     */
    private BigInteger workflowTaskBatchId;

    private BigInteger workflowNodeId;

    /**
     * 工作流上下文
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

    public List<JobTask> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<JobTask> taskList) {
        this.taskList = taskList;
    }

    public String getArgsStr() {
        return argsStr;
    }

    public void setArgsStr(String argsStr) {
        this.argsStr = argsStr;
    }

    public JobArgsType getArgsType() {
        return argsType;
    }

    public void setArgsType(JobArgsType argsType) {
        this.argsType = argsType;
    }

    public Integer getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(Integer routeKey) {
        this.routeKey = routeKey;
    }

    public String getExtAttrs() {
        return extAttrs;
    }

    public void setExtAttrs(String extAttrs) {
        this.extAttrs = extAttrs;
    }

    public BigInteger getTaskId() {
        return taskId;
    }

    public void setTaskId(BigInteger taskId) {
        this.taskId = taskId;
    }

    public Integer getParallelNum() {
        return parallelNum;
    }

    public void setParallelNum(Integer parallelNum) {
        this.parallelNum = parallelNum;
    }

    public ExecutorType getExecutorType() {
        return executorType;
    }

    public void setExecutorType(ExecutorType executorType) {
        this.executorType = executorType;
    }

    public String getExecutorInfo() {
        return executorInfo;
    }

    public void setExecutorInfo(String executorInfo) {
        this.executorInfo = executorInfo;
    }

    public Integer getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public void setMaxRetryTimes(Integer maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    public Integer getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(Integer retryInterval) {
        this.retryInterval = retryInterval;
    }

    public Integer getShardingTotal() {
        return shardingTotal;
    }

    public void setShardingTotal(Integer shardingTotal) {
        this.shardingTotal = shardingTotal;
    }

    public Integer getShardingIndex() {
        return shardingIndex;
    }

    public void setShardingIndex(Integer shardingIndex) {
        this.shardingIndex = shardingIndex;
    }

    public Integer getExecutorTimeout() {
        return executorTimeout;
    }

    public void setExecutorTimeout(Integer executorTimeout) {
        this.executorTimeout = executorTimeout;
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

    public String getWfContext() {
        return wfContext;
    }

    public void setWfContext(String wfContext) {
        this.wfContext = wfContext;
    }
}
