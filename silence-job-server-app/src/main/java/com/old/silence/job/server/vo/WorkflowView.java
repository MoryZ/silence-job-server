package com.old.silence.job.server.vo;

import com.old.silence.data.commons.domain.AuditableView;
import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.common.enums.TriggerType;

import java.math.BigInteger;
import java.util.List;

/**
 * @author moryzang
 */
public interface WorkflowView extends AuditableView {

    BigInteger getId();

    String getWorkflowName();

    String getGroupName();

    TriggerType getTriggerType();

    JobBlockStrategy getBlockStrategy();

    String getTriggerInterval();

    Integer getExecutorTimeout();

    Boolean getWorkflowStatus();

    Long getNextTriggerAt();

    String getFlowInfo();

    Integer getBucketIndex();

    String getDescription();

    String getWfContext();

    Integer getVersion();

    String getExtAttrs();

    BigInteger getOwnerId();

    List<WorkflowNotifyConfigRelationView> getNotifyRelations();
}
