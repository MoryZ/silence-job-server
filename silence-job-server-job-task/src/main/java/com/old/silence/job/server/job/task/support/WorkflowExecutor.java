package com.old.silence.job.server.job.task.support;

import com.old.silence.job.common.enums.WorkflowNodeType;
import com.old.silence.job.server.job.task.support.executor.workflow.WorkflowExecutorContext;


public interface WorkflowExecutor {

    WorkflowNodeType getWorkflowNodeType();

    void execute(WorkflowExecutorContext context);
}
