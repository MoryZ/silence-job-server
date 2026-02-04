package com.old.silence.job.server.job.task.support.prepare.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.old.silence.job.server.job.task.support.WorkflowTaskConverter;
import com.old.silence.job.server.job.task.support.generator.batch.WorkflowBatchGenerator;

import java.util.Objects;


@Component

public class TerminalWorkflowPrepareHandler extends AbstractWorkflowPrePareHandler {


    private static final Logger log = LoggerFactory.getLogger(TerminalWorkflowPrepareHandler.class);
    private final WorkflowBatchGenerator workflowBatchGenerator;

    public TerminalWorkflowPrepareHandler(WorkflowBatchGenerator workflowBatchGenerator) {
        this.workflowBatchGenerator = workflowBatchGenerator;
    }

    @Override
    public boolean matches(JobTaskBatchStatus status) {
        return Objects.isNull(status);
    }

    @Override
    protected void doHandler(WorkflowTaskPrepareDTO jobPrepareDTO) {
        log.debug("无处理中的工作流数据. workflowId:[{}]", jobPrepareDTO.getWorkflowId());
        workflowBatchGenerator.generateJobTaskBatch(WorkflowTaskConverter.INSTANCE.toWorkflowTaskBatchGeneratorContext(jobPrepareDTO));
    }
}
