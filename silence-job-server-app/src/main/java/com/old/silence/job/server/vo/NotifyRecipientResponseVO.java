package com.old.silence.job.server.vo;



import java.math.BigInteger;
import java.time.Instant;

import com.old.silence.job.common.enums.NotifyType;


public class NotifyRecipientResponseVO {

    private BigInteger id;

    /**
     * 接收人名称
     */
    private String recipientName;

    /**
     * 通知类型 1、钉钉 2、邮件 3、企业微信 4 飞书
     */
    private NotifyType notifyType;

    /**
     * 配置属性
     */
    private String notifyAttribute;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Instant createdDate;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public NotifyType getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(NotifyType notifyType) {
        this.notifyType = notifyType;
    }

    public String getNotifyAttribute() {
        return notifyAttribute;
    }

    public void setNotifyAttribute(String notifyAttribute) {
        this.notifyAttribute = notifyAttribute;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }
}
