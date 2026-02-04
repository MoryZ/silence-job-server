package com.old.silence.job.server.vo;



/**
 * @author MurrayZhang
 */

public class MqStatVo {
    private Integer topicCount;
    private Integer publishRelationCount;

    public Integer getTopicCount() {
        return topicCount;
    }

    public void setTopicCount(Integer topicCount) {
        this.topicCount = topicCount;
    }

    public Integer getPublishRelationCount() {
        return publishRelationCount;
    }

    public void setPublishRelationCount(Integer publishRelationCount) {
        this.publishRelationCount = publishRelationCount;
    }
}
