package com.old.silence.job.server.job.task.dto;

import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.common.enums.TriggerType;
import com.old.silence.job.server.common.dto.PartitionTask;


public class JobPartitionTaskDTO extends PartitionTask {

    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 下次触发时间
     */
    private long nextTriggerAt;

    /**
     * 阻塞策略 1、丢弃 2、覆盖 3、并行
     */
    private JobBlockStrategy blockStrategy;

    /**
     * 触发类型 1.CRON 表达式 2. 固定时间
     */
    private TriggerType triggerType;

    /**
     * 间隔时长
     */
    private String triggerInterval;

    /**
     * 任务执行超时时间，单位秒
     */
    private Integer executorTimeout;

    /**
     * 任务类型
     */
    private JobTaskType taskType;

    /**
     * 是否是常驻任务
     */
    private Boolean resident;

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

    public long getNextTriggerAt() {
        return nextTriggerAt;
    }

    public void setNextTriggerAt(long nextTriggerAt) {
        this.nextTriggerAt = nextTriggerAt;
    }

    public JobBlockStrategy getBlockStrategy() {
        return blockStrategy;
    }

    public void setBlockStrategy(JobBlockStrategy blockStrategy) {
        this.blockStrategy = blockStrategy;
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

    public JobTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(JobTaskType taskType) {
        this.taskType = taskType;
    }

    public Boolean getResident() {
        return resident;
    }

    public void setResident(Boolean resident) {
        this.resident = resident;
    }
}
