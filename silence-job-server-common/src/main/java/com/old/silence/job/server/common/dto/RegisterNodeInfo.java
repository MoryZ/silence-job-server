package com.old.silence.job.server.common.dto;



import com.old.silence.job.common.enums.NodeType;

import java.text.MessageFormat;
import java.time.Instant;

/**
 * 注册的节点信息
 *
 */

public class RegisterNodeInfo implements Comparable<RegisterNodeInfo> {

    private String namespaceId;

    private String groupName;

    private String hostId;

    private String hostIp;

    private Integer hostPort;

    private Instant expireAt;

    private NodeType nodeType;

    private String contextPath;

    public String address() {
        return MessageFormat.format("{0}:{1}", hostIp, hostPort.toString());
    }

    @Override
    public int compareTo(RegisterNodeInfo info) {
        return hostId.compareTo(info.hostId);
    }

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

    public Instant getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Instant expireAt) {
        this.expireAt = expireAt;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
}
