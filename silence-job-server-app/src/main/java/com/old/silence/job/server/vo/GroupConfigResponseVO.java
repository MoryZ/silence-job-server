package com.old.silence.job.server.vo;

import com.old.silence.job.common.enums.IdGeneratorMode;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;



public class GroupConfigResponseVO {

    private BigInteger id;

    private String groupName;

    private String namespaceId;

    private String namespaceName;

    private Boolean groupStatus;

    private Integer groupPartition;

    private Integer routeKey;

    private Integer version;

    private String description;

    private IdGeneratorMode idGeneratorMode;

    private String idGeneratorModeName;

    private Boolean initScene;

    private List<String> onlinePodList;

    private String token;

    private Instant createdDate;

    private Instant updatedDate;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    public Boolean getGroupStatus() {
        return groupStatus;
    }

    public void setGroupStatus(Boolean groupStatus) {
        this.groupStatus = groupStatus;
    }

    public Integer getGroupPartition() {
        return groupPartition;
    }

    public void setGroupPartition(Integer groupPartition) {
        this.groupPartition = groupPartition;
    }

    public Integer getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(Integer routeKey) {
        this.routeKey = routeKey;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IdGeneratorMode getIdGeneratorMode() {
        return idGeneratorMode;
    }

    public void setIdGeneratorMode(IdGeneratorMode idGeneratorMode) {
        this.idGeneratorMode = idGeneratorMode;
    }

    public String getIdGeneratorModeName() {
        return idGeneratorModeName;
    }

    public void setIdGeneratorModeName(String idGeneratorModeName) {
        this.idGeneratorModeName = idGeneratorModeName;
    }

    public Boolean getInitScene() {
        return initScene;
    }

    public void setInitScene(Boolean initScene) {
        this.initScene = initScene;
    }

    public List<String> getOnlinePodList() {
        return onlinePodList;
    }

    public void setOnlinePodList(List<String> onlinePodList) {
        this.onlinePodList = onlinePodList;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
}
