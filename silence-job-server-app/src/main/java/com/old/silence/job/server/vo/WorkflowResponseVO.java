package com.old.silence.job.server.vo;


import com.old.silence.job.common.enums.TriggerType;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Set;


public class WorkflowResponseVO {

    private BigInteger id;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 触发类型
     */
    private TriggerType triggerType;

    /**
     * 触发间隔
     */
    private String triggerInterval;

    /**
     * 执行超时时间
     */
    private Integer executorTimeout;

    /**
     * 工作流状态 0、关闭、1、开启
     */
    private Boolean workflowStatus;

    /**
     * 任务执行时间
     */
    private Instant nextTriggerAt;

    /**
     * 创建时间
     */
    private Instant createdDate;

    /**
     * 修改时间
     */
    private Instant updatedDate;

    /**
     * 通知告警场景配置id列表
     */
    private Set<BigInteger> notifyIds;

    /**
     * 工作流上下文
     */
    private String wfContext;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggerInterval() {
        return triggerInterval;
    }

    public void setTriggerInterval(String triggerInterval) {
        this.triggerInterval = triggerInterval;
    }

    public Integer getExecutorTimeout() {
        return executorTimeout;
    }

    public void setExecutorTimeout(Integer executorTimeout) {
        this.executorTimeout = executorTimeout;
    }

    public Boolean getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(Boolean workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public Instant getNextTriggerAt() {
        return nextTriggerAt;
    }

    public void setNextTriggerAt(Instant nextTriggerAt) {
        this.nextTriggerAt = nextTriggerAt;
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

    public String getWfContext() {
        return wfContext;
    }

    public void setWfContext(String wfContext) {
        this.wfContext = wfContext;
    }
}
