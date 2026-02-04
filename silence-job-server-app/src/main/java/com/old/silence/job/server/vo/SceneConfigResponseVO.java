package com.old.silence.job.server.vo;


import com.old.silence.job.common.enums.BackoffType;
import com.old.silence.job.common.enums.CbTriggerType;
import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.common.enums.TriggerType;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Set;


public class SceneConfigResponseVO {

    private BigInteger id;

    private String groupName;

    private String sceneName;

    private Boolean sceneStatus;

    private Integer maxRetryCount;

    private BackoffType backOff;

    private String triggerInterval;

    private String description;

    private Long deadlineRequest;

    private Integer routeKey;

    private JobBlockStrategy blockStrategy;

    private Integer executorTimeout;

    private Instant createdDate;

    private Instant updatedDate;

    /**
     * 通知告警场景配置id列表
     */
    private Set<BigInteger> notifyIds;

    /**
     * 回调状态 0、不开启 1、开启
     */
    private Boolean cbStatus;

    /**
     * 回调触发类型
     */
    private CbTriggerType cbTriggerType;

    /**
     * 回调的最大执行次数
     */
    private int cbMaxCount;

    /**
     * 回调间隔时间
     */
    private String cbTriggerInterval;

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

    public Boolean getSceneStatus() {
        return sceneStatus;
    }

    public void setSceneStatus(Boolean sceneStatus) {
        this.sceneStatus = sceneStatus;
    }

    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public BackoffType getBackOff() {
        return backOff;
    }

    public void setBackOff(BackoffType backOff) {
        this.backOff = backOff;
    }

    public String getTriggerInterval() {
        return triggerInterval;
    }

    public void setTriggerInterval(String triggerInterval) {
        this.triggerInterval = triggerInterval;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDeadlineRequest() {
        return deadlineRequest;
    }

    public void setDeadlineRequest(Long deadlineRequest) {
        this.deadlineRequest = deadlineRequest;
    }

    public Integer getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(Integer routeKey) {
        this.routeKey = routeKey;
    }

    public JobBlockStrategy getBlockStrategy() {
        return blockStrategy;
    }

    public void setBlockStrategy(JobBlockStrategy blockStrategy) {
        this.blockStrategy = blockStrategy;
    }

    public Integer getExecutorTimeout() {
        return executorTimeout;
    }

    public void setExecutorTimeout(Integer executorTimeout) {
        this.executorTimeout = executorTimeout;
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

    public Set<BigInteger> getNotifyIds() {
        return notifyIds;
    }

    public void setNotifyIds(Set<BigInteger> notifyIds) {
        this.notifyIds = notifyIds;
    }

    public Boolean getCbStatus() {
        return cbStatus;
    }

    public void setCbStatus(Boolean cbStatus) {
        this.cbStatus = cbStatus;
    }

    public CbTriggerType getCbTriggerType() {
        return cbTriggerType;
    }

    public void setCbTriggerType(CbTriggerType cbTriggerType) {
        this.cbTriggerType = cbTriggerType;
    }

    public int getCbMaxCount() {
        return cbMaxCount;
    }

    public void setCbMaxCount(int cbMaxCount) {
        this.cbMaxCount = cbMaxCount;
    }

    public String getCbTriggerInterval() {
        return cbTriggerInterval;
    }

    public void setCbTriggerInterval(String cbTriggerInterval) {
        this.cbTriggerInterval = cbTriggerInterval;
    }
}
