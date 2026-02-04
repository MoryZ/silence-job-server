package com.old.silence.job.server.vo;



import java.math.BigInteger;
import java.time.Instant;


public class RetryDeadLetterResponseVO {

    private BigInteger id;

    private String groupName;

    private String sceneName;

    private String idempotentId;

    private String bizNo;

    private String executorName;

    private String argsStr;

    private String extAttrs;

    private Integer taskType;

    private String uniqueId;

    private Instant createdDate;

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

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public String getIdempotentId() {
        return idempotentId;
    }

    public void setIdempotentId(String idempotentId) {
        this.idempotentId = idempotentId;
    }

    public String getBizNo() {
        return bizNo;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }

    public String getArgsStr() {
        return argsStr;
    }

    public void setArgsStr(String argsStr) {
        this.argsStr = argsStr;
    }

    public String getExtAttrs() {
        return extAttrs;
    }

    public void setExtAttrs(String extAttrs) {
        this.extAttrs = extAttrs;
    }

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }
}
