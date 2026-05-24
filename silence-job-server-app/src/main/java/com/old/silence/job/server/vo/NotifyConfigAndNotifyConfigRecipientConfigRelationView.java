package com.old.silence.job.server.vo;


import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.SystemTaskType;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;


public interface NotifyConfigAndNotifyConfigRecipientConfigRelationView {

    BigInteger getId();

    String getGroupName();

    String getBusinessId();

    String getBusinessName();

    SystemTaskType getSystemTaskType();

    Boolean getNotifyStatus();

    String getNotifyName();

    Set<BigInteger> getRecipientIds();

    Set<String> getRecipientNames();

    Integer getNotifyThreshold();

    JobNotifyScene getNotifyScene();

    Boolean getRateLimiterStatus();

    Integer getRateLimiterThreshold();

    String getDescription();

    List<NotifyConfigRecipientConfigRelationView> getRecipientRelations();

}
