package com.old.silence.job.server.vo;



import com.old.silence.job.common.enums.NodeType;

import java.time.Instant;
import java.util.Set;


public class ServerNodeResponseVO {

    private String groupName;

    private String hostId;

    private String hostIp;

    private Integer hostPort;

    private NodeType nodeType;

    private Instant createdDate;

    private Instant updatedDate;

    private String extAttrs;

    private Set<Integer> consumerBuckets;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public Integer getHostPort() {
        return hostPort;
    }

    public void setHostPort(Integer hostPort) {
        this.hostPort = hostPort;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
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

    public String getExtAttrs() {
        return extAttrs;
    }

    public void setExtAttrs(String extAttrs) {
        this.extAttrs = extAttrs;
    }

    public Set<Integer> getConsumerBuckets() {
        return consumerBuckets;
    }

    public void setConsumerBuckets(Set<Integer> consumerBuckets) {
        this.consumerBuckets = consumerBuckets;
    }
}
