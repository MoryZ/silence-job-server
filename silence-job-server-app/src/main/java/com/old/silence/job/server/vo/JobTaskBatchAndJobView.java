package com.old.silence.job.server.vo;

import com.old.silence.data.commons.domain.AuditableView;
import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.common.enums.SystemTaskType;

import java.math.BigInteger;


public interface JobTaskBatchAndJobView extends AuditableView {

    BigInteger getId();

    String getGroupName();

    BigInteger getJobId();

    BigInteger getWorkflowTaskBatchId();

    BigInteger getWorkflowNodeId();

    BigInteger getParentWorkflowNodeId();

    JobTaskBatchStatus getTaskBatchStatus();

    Long getExecutionAt();

    SystemTaskType getSystemTaskType();

    JobOperationReason getOperationReason();

    JobView getJob();
}
