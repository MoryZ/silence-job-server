package com.old.silence.job.server.dto;


import com.old.silence.data.commons.annotation.RelationalQueryProperty;
import com.old.silence.data.commons.converter.Part;

public class RetryDeadLetterQuery {
    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private String groupName;
    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private String sceneName;
    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private String bizNo;
    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private String idempotentId;


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

    public String getBizNo() {
        return bizNo;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }

    public String getIdempotentId() {
        return idempotentId;
    }

    public void setIdempotentId(String idempotentId) {
        this.idempotentId = idempotentId;
    }

}
