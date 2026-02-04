package com.old.silence.job.server.dto;



import java.math.BigInteger;
import java.util.Set;

public class ExportSceneCommand {

    private String groupName;

    private Boolean sceneStatus;

    private String sceneName;

    private Set<BigInteger> sceneIds;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Boolean getSceneStatus() {
        return sceneStatus;
    }

    public void setSceneStatus(Boolean sceneStatus) {
        this.sceneStatus = sceneStatus;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public Set<BigInteger> getSceneIds() {
        return sceneIds;
    }

    public void setSceneIds(Set<BigInteger> sceneIds) {
        this.sceneIds = sceneIds;
    }
}
