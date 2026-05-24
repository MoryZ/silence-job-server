package com.old.silence.job.server.vo;

import com.old.silence.job.common.enums.ExecutorType;
import com.old.silence.job.common.enums.JobArgsType;
import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.common.enums.TriggerType;

import java.math.BigInteger;


public interface JobView {

    BigInteger getJobId();

    String getGroupName();

    String getJobName();

    String getArgsStr();

    JobArgsType getArgsType();

    String getExtAttrs();

    Long getNextTriggerAt();

    Boolean getJobStatus();

    Integer getRouteKey();

    ExecutorType getExecutorType();

    String getExecutorInfo();

    TriggerType getTriggerType();

    String getTriggerInterval();

    JobBlockStrategy getBlockStrategy();

    Integer getExecutorTimeout();

    Integer getMaxRetryTimes();

    Integer getRetryInterval();

    JobTaskType getTaskType();

    Integer getParallelNum();

    Integer getBucketIndex();

    Boolean getResident();

    String getDescription();

    BigInteger getOwnerId();

    String getOwnerName();
}
