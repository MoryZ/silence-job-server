package com.old.silence.job.server.dto;

import java.util.Set;


public class ExportGroupCommand {

    private String groupName;

    private Boolean groupStatus;

    private Set<Long> groupIds;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Boolean getGroupStatus() {
        return groupStatus;
    }

    public void setGroupStatus(Boolean groupStatus) {
        this.groupStatus = groupStatus;
    }

    public Set<Long> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(Set<Long> groupIds) {
        this.groupIds = groupIds;
    }
}
