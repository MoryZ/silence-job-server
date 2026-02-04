package com.old.silence.job.server.dto;


import com.old.silence.data.commons.annotation.RelationalQueryProperty;
import com.old.silence.data.commons.converter.Part;
import com.old.silence.job.common.enums.SystemTaskType;

public class NotifyConfigQuery {
    @RelationalQueryProperty(type = Part.Type.CONTAINING)
    private String groupName;
    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private SystemTaskType systemTaskType;
    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private Boolean notifyStatus;
    @RelationalQueryProperty(type = Part.Type.STARTING_WITH)
    private String notifyName;

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
}
