package com.old.silence.job.server.dto;


import com.old.silence.data.commons.annotation.RelationalQueryProperty;
import com.old.silence.data.commons.converter.Part;

public class SceneConfigQuery {

    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private String groupName;
    @RelationalQueryProperty(type = Part.Type.CONTAINING)
    private String sceneName;
    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private Boolean sceneStatus;

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
}
