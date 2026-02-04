package com.old.silence.job.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.old.silence.job.common.enums.JobArgsType;
import com.old.silence.job.common.enums.JobTaskStatus;

import java.math.BigInteger;
import java.time.Instant;



public class JobTaskResponseVO {

    private BigInteger id;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务信息id
     */
    private BigInteger jobId;

    /**
     * 调度任务id
     */
    private BigInteger taskBatchId;

    /**
     * 父执行器id
     */
    private BigInteger parentId;

    /**
     * 执行的状态 0、失败 1、成功
     */
    private JobTaskStatus taskStatus;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 执行结果
     */
    private String resultMessage;

    /**
     * 客户端ID
     */
    private String clientInfo;

    /**
     * 执行方法参数
     */
    private String argsStr;

    /**
     * 参数类型 text/json
     */
    private JobArgsType argsType;

    /**
     * 扩展字段
     */
    private String extAttrs;

    /**
     * 创建时间
     */
    private Instant createdDate;

    /**
     * 修改时间
     */
    private Instant updatedDate;

    /**
     * 是否有子节点
     */
    @JsonProperty("isLeaf")
    private boolean isChildNode;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
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

    public BigInteger getParentId() {
        return parentId;
    }

    public void setParentId(BigInteger parentId) {
        this.parentId = parentId;
    }

    public JobTaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(JobTaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(String clientInfo) {
        this.clientInfo = clientInfo;
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

    public String getExtAttrs() {
        return extAttrs;
    }

    public void setExtAttrs(String extAttrs) {
        this.extAttrs = extAttrs;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    public boolean isChildNode() {
        return isChildNode;
    }

    public void setChildNode(boolean childNode) {
        isChildNode = childNode;
    }
}
