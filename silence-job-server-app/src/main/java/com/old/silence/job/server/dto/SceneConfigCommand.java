package com.old.silence.job.server.dto;

import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.enums.BackoffType;
import com.old.silence.job.common.enums.CbTriggerType;
import com.old.silence.job.common.enums.RetryBlockStrategy;
import com.old.silence.job.common.enums.TriggerType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigInteger;
import java.util.Set;



public class SceneConfigCommand {

    @NotBlank(message = "组名称 不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母、下划线和短横线")
    private String groupName;

    @NotBlank(message = "场景名称不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母、下划线和短横线")
    private String sceneName;

    @NotNull(message = "场景状态不能为空")
    private Boolean sceneStatus;

    @Max(message = "最大重试次数", value = 9999999)
    @Min(message = "最小重试次数", value = 0)
    private Integer maxRetryCount;

    @NotNull(message = "退避策略不能为空 1、默认等级 2、固定间隔时间 3、CRON 表达式")
    private BackoffType backOff;

    @NotNull(message = "路由策略不能为空")
    private Integer routeKey;

    /**
     * @see: RetryBlockStrategy
     */
    @NotNull(message = "阻塞策略不能为空")
    private RetryBlockStrategy blockStrategy;

    /**
     * 描述
     */
    private String description;

    /**
     * 退避策略为固定间隔时间必填
     */
    private String triggerInterval;

    /**
     * Deadline Request 调用链超时 单位毫秒
     * 默认值为 60*10*1000
     */
    @Max(message = "最大60000毫秒", value = SystemConstants.DEFAULT_DDL)
    @Min(message = "最小100ms", value = 100)
    @NotNull(message = "调用链超时不能为空")
    private Long deadlineRequest;

    @Max(message = "最大60(秒)", value = 60)
    @Min(message = "最小1(秒)", value = 1)
    @NotNull(message = "执行超时不能为空")
    private Integer executorTimeout;

    /**
     * 是否删除
     */
    private Integer isDeleted;

    /**
     * 通知告警场景配置id列表
     */
    private Set<BigInteger> notifyIds;

    /**
     * 回调状态 0、不开启 1、开启
     */
    @NotNull(message = "回调状态不能为空")
    private Boolean cbStatus;

    /**
     * 回调触发类型
     */
    @NotNull(message = "回调触发类型不能为空")
    private CbTriggerType cbTriggerType;

    /**
     * 回调的最大执行次数
     */
    @NotNull(message = "回调的最大执行次数不能为空")
    private int cbMaxCount;

    /**
     * 回调间隔时间
     */
    private String cbTriggerInterval;

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

    public Integer getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(Integer routeKey) {
        this.routeKey = routeKey;
    }

    public @NotNull(message = "阻塞策略不能为空") RetryBlockStrategy getBlockStrategy() {
        return blockStrategy;
    }

    public void setBlockStrategy(@NotNull(message = "阻塞策略不能为空") RetryBlockStrategy blockStrategy) {
        this.blockStrategy = blockStrategy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTriggerInterval() {
        return triggerInterval;
    }

    public void setTriggerInterval(String triggerInterval) {
        this.triggerInterval = triggerInterval;
    }

    public Long getDeadlineRequest() {
        return deadlineRequest;
    }

    public void setDeadlineRequest(Long deadlineRequest) {
        this.deadlineRequest = deadlineRequest;
    }

    public Integer getExecutorTimeout() {
        return executorTimeout;
    }

    public void setExecutorTimeout(Integer executorTimeout) {
        this.executorTimeout = executorTimeout;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
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
