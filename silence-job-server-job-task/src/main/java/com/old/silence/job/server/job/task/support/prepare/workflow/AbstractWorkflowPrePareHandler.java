package com.old.silence.job.server.job.task.support.prepare.workflow;

import com.old.silence.job.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.old.silence.job.server.job.task.support.WorkflowPrePareHandler;

public abstract class AbstractWorkflowPrePareHandler implements WorkflowPrePareHandler {

    @Override
    public void handler(WorkflowTaskPrepareDTO workflowTaskPrepareDTO) {

        doHandler(workflowTaskPrepareDTO);
    }

    protected abstract void doHandler(WorkflowTaskPrepareDTO jobPrepareDTO);

}
