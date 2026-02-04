package com.old.silence.job.server.dto;


import com.old.silence.data.commons.annotation.RelationalQueryProperty;
import com.old.silence.data.commons.converter.Part;

public class GroupConfigQuery {

    @RelationalQueryProperty(type = Part.Type.STARTING_WITH)
    private String groupName;

    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private Boolean groupStatus;

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
}
