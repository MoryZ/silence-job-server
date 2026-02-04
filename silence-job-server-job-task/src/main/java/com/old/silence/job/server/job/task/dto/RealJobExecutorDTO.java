package com.old.silence.job.server.job.task.dto;


import com.old.silence.job.common.enums.ExecutorType;
import com.old.silence.job.common.enums.JobArgsType;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.common.enums.MapReduceStage;

import java.math.BigInteger;

public class RealJobExecutorDTO extends BaseDTO {

    private BigInteger jobId;

    /**
     * 名称
     */
    private String jobName;

    /**
     * 执行方法参数
     */
    private String argsStr;

    /**
     * 参数类型 text/json
     */
    private JobArgsType argsType;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 动态分片的阶段
     * {@link MapReduceStage}
     */
    private MapReduceStage mrStage;

    /**
     * 扩展字段
     */
    private String extAttrs;


    private BigInteger taskBatchId;

    private BigInteger taskId;

    private JobTaskType taskType;

    private String groupName;

    private Integer parallelNum;

    private ExecutorType executorType;

    private String executorInfo;

    private String clientId;

    /**
     * 最大重试次数
     */
    private Integer maxRetryTimes;

    /**
     * 重试间隔(s)
     */
    private Integer retryInterval;

    private Integer retryCount;

    private Integer shardingTotal;

    private Integer shardingIndex;

    private Integer executorTimeout;

    /**
     * 工作流任务批次id
     */
    private BigInteger workflowTaskBatchId;

    private BigInteger workflowNodeId;

    /**
     * 重试场景 auto、manual
     */
    private Integer retryScene;

    /**
     * 是否是重试流量
     */
    private Boolean retryStatus = Boolean.FALSE;

    /**
     * 工作流上下文
     */
    private String wfContext;

    @Override
    public BigInteger getJobId() {
        return jobId;
    }

    @Override
    public void setJobId(BigInteger jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
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

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public MapReduceStage getMrStage() {
        return mrStage;
    }

    public void setMrStage(MapReduceStage mrStage) {
        this.mrStage = mrStage;
    }

    public String getExtAttrs() {
        return extAttrs;
    }

    public void setExtAttrs(String extAttrs) {
        this.extAttrs = extAttrs;
    }

    @Override
    public BigInteger getTaskBatchId() {
        return taskBatchId;
    }

    @Override
    public void setTaskBatchId(BigInteger taskBatchId) {
        this.taskBatchId = taskBatchId;
    }

    @Override
    public BigInteger getTaskId() {
        return taskId;
    }

    @Override
    public void setTaskId(BigInteger taskId) {
        this.taskId = taskId;
    }

    @Override
    public JobTaskType getTaskType() {
        return taskType;
    }

    @Override
    public void setTaskType(JobTaskType taskType) {
        this.taskType = taskType;
    }

    @Override
    public String getGroupName() {
        return groupName;
    }

    @Override
    public void setGroupName(String groupName) {
        this.groupName = groupName;
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

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public void setClientId(String clientId) {
        this.clientId = clientId;
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

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
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

    public Integer getRetryScene() {
        return retryScene;
    }

    public void setRetryScene(Integer retryScene) {
        this.retryScene = retryScene;
    }

    public Boolean getRetryStatus() {
        return retryStatus;
    }

    public void setRetryStatus(Boolean retryStatus) {
        this.retryStatus = retryStatus;
    }

    public String getWfContext() {
        return wfContext;
    }

    public void setWfContext(String wfContext) {
        this.wfContext = wfContext;
    }
}
