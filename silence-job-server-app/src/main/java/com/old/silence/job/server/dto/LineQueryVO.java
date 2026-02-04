package com.old.silence.job.server.dto;


public class LineQueryVO  {
    /**
     * 组名称
     */
    private String groupName;

    /**
     * 时间间隔类型
     */
    private String type = "WEEK";

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
