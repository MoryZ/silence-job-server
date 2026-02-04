package com.old.silence.job.server.common.dto;


import com.old.silence.job.log.enums.LogTypeEnum;

public class LogMetaDTO {

    /**
     * 命名空间
     */
    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 时间
     */
    private Long timestamp;

    /**
     * 日志类型
     */
    private LogTypeEnum logType;

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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public LogTypeEnum getLogType() {
        return logType;
    }

    public void setLogType(LogTypeEnum logType) {
        this.logType = logType;
    }
}
