package com.old.silence.job.server.vo;

import com.old.silence.job.common.enums.BackoffType;
import com.old.silence.job.common.enums.CbTriggerType;
import com.old.silence.job.common.enums.RetryBlockStrategy;

import java.math.BigInteger;
import java.util.List;


public interface RetrySceneConfigAndRetrySceneConfigNotifyConfigRelationView {

    BigInteger getId();

    String getGroupName();

    String getSceneName();

    RetryBlockStrategy getBlockStrategy();

    Boolean getSceneStatus();

    Integer getMaxRetryCount();

    BackoffType getBackOff();

    String getTriggerInterval();

    String getDescription();

    Long getDeadlineRequest();

    Integer getRouteKey();

    Integer getExecutorTimeout();

    Boolean getCbStatus();

    CbTriggerType getCbTriggerType();

    int getCbMaxCount();

    String getCbTriggerInterval();

    List<RetrySceneConfigNotifyConfigRelationView> getNotifyRelations();
}
