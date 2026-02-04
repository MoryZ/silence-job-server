package com.old.silence.job.server.job.task.support.executor.workflow;

import com.old.silence.job.common.enums.WorkflowNodeType;
import com.old.silence.job.server.job.task.support.WorkflowExecutor;

import java.util.concurrent.ConcurrentHashMap;


public class WorkflowExecutorFactory {

    private static final ConcurrentHashMap<WorkflowNodeType, WorkflowExecutor> CACHE = new ConcurrentHashMap<>();

    protected static void registerJobExecutor(WorkflowNodeType workflowNodeType, WorkflowExecutor executor) {
        CACHE.put(workflowNodeType, executor);
    }

    public static WorkflowExecutor getWorkflowExecutor(WorkflowNodeType workflowNodeType) {
        return CACHE.get(workflowNodeType);
    }
}
