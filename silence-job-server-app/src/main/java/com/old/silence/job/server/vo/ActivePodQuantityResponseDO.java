package com.old.silence.job.server.vo;


import com.old.silence.job.common.enums.NodeType;


public class ActivePodQuantityResponseDO {

    private Long total;

    private NodeType nodeType;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }
}
