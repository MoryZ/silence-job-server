package com.old.silence.job.server.dto;

import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.SystemTaskType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigInteger;
import java.util.Set;



public class NotifyConfigCommand {

    private BigInteger id;

    @NotBlank(message = "组名称 不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母、下划线和短横线")
    private String groupName;

    /**
     * 任务类型 1、重试任务 2、回调任务、 3、JOB任务 4、WORKFLOW任务
     */
    @NotNull(message = "任务类型不能为空")
    private SystemTaskType systemTaskType;

    @NotNull(message = "通知状态不能为空")
    private Boolean notifyStatus;

    @NotNull(message = "通知告警场景名不能为空")
    private String notifyName;

    @NotEmpty(message = "通知人列表")
    private Set<Long> recipientIds;

    private Integer notifyThreshold;

    @NotNull(message = "通知场景不能为空")
    private JobNotifyScene notifyScene;

    @NotNull(message = "限流状态不能为空")
    private Boolean rateLimiterStatus;

    private Integer rateLimiterThreshold;
    /**
     * 描述
     */
    private String description;

    /**
     * 是否删除
     */
    private Boolean isDeleted;

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

    public SystemTaskType getSystemTaskType() {
        return systemTaskType;
    }

    public void setSystemTaskType(SystemTaskType systemTaskType) {
        this.systemTaskType = systemTaskType;
    }

    public Boolean getNotifyStatus() {
        return notifyStatus;
    }

    public void setNotifyStatus(Boolean notifyStatus) {
        this.notifyStatus = notifyStatus;
    }

    public String getNotifyName() {
        return notifyName;
    }

    public void setNotifyName(String notifyName) {
        this.notifyName = notifyName;
    }

    public Set<Long> getRecipientIds() {
        return recipientIds;
    }

    public void setRecipientIds(Set<Long> recipientIds) {
        this.recipientIds = recipientIds;
    }

    public Integer getNotifyThreshold() {
        return notifyThreshold;
    }

    public void setNotifyThreshold(Integer notifyThreshold) {
        this.notifyThreshold = notifyThreshold;
    }

    public JobNotifyScene getNotifyScene() {
        return notifyScene;
    }

    public void setNotifyScene(JobNotifyScene notifyScene) {
        this.notifyScene = notifyScene;
    }

    public Boolean getRateLimiterStatus() {
        return rateLimiterStatus;
    }

    public void setRateLimiterStatus(Boolean rateLimiterStatus) {
        this.rateLimiterStatus = rateLimiterStatus;
    }

    public Integer getRateLimiterThreshold() {
        return rateLimiterThreshold;
    }

    public void setRateLimiterThreshold(Integer rateLimiterThreshold) {
        this.rateLimiterThreshold = rateLimiterThreshold;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
