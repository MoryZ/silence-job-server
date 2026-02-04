package com.old.silence.job.server.dto;


import java.math.BigInteger;
import java.util.Set;



public class ExportNotifyRecipientCommand {

    private Set<BigInteger> notifyRecipientIds;

    private Integer notifyType;

    private String recipientName;

    public Set<BigInteger> getNotifyRecipientIds() {
        return notifyRecipientIds;
    }

    public void setNotifyRecipientIds(Set<BigInteger> notifyRecipientIds) {
        this.notifyRecipientIds = notifyRecipientIds;
    }

    public Integer getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(Integer notifyType) {
        this.notifyType = notifyType;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }
}
