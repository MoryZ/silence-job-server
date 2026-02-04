package com.old.silence.job.server.job.task.support;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import com.old.silence.job.server.common.dto.WorkflowAlarmInfo;
import com.old.silence.job.server.domain.model.Workflow;
import com.old.silence.job.server.domain.model.WorkflowNode;
import com.old.silence.job.server.domain.model.WorkflowTaskBatch;
import com.old.silence.job.server.job.task.dto.WorkflowPartitionTaskDTO;
import com.old.silence.job.server.job.task.dto.WorkflowTaskFailAlarmEventDTO;
import com.old.silence.job.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.old.silence.job.server.job.task.support.block.workflow.WorkflowBlockStrategyContext;
import com.old.silence.job.server.job.task.support.executor.workflow.WorkflowExecutorContext;
import com.old.silence.job.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;
import com.old.silence.job.server.job.task.support.generator.batch.WorkflowTaskBatchGeneratorContext;

import java.util.List;


@Mapper
public interface WorkflowTaskConverter {
    WorkflowTaskConverter INSTANCE = Mappers.getMapper(WorkflowTaskConverter.class);

    WorkflowPartitionTaskDTO toWorkflowPartitionTask(Workflow workflow);

    @Mapping(source = "id", target = "workflowId")
    WorkflowTaskPrepareDTO toWorkflowTaskPrepareDTO(WorkflowPartitionTaskDTO workflowPartitionTaskDTO);

    @Mapping(source = "id", target = "workflowId")
    WorkflowTaskPrepareDTO toWorkflowTaskPrepareDTO(Workflow workflow);

    WorkflowTaskBatchGeneratorContext toWorkflowTaskBatchGeneratorContext(WorkflowTaskPrepareDTO workflowTaskPrepareDTO);

    WorkflowTaskBatch toWorkflowTaskBatch(WorkflowTaskBatchGeneratorContext context);

    JobTaskBatchGeneratorContext toJobTaskBatchGeneratorContext(WorkflowExecutorContext context);

    @Mapping(source = "id", target = "workflowNodeId")
    WorkflowExecutorContext toWorkflowExecutorContext(WorkflowNode workflowNode);

    WorkflowTaskBatchGeneratorContext toWorkflowTaskBatchGeneratorContext(WorkflowBlockStrategyContext context);

    WorkflowBlockStrategyContext toWorkflowBlockStrategyContext(WorkflowTaskPrepareDTO prepareDTO);

    List<WorkflowAlarmInfo> toWorkflowTaskFailAlarmEventDTO(List<WorkflowTaskFailAlarmEventDTO> workflowTaskFailAlarmEventDTOList);

    @Mapping(source = "workflowTaskBatchId", target = "id")
    WorkflowAlarmInfo toWorkflowTaskFailAlarmEventDTO(WorkflowTaskFailAlarmEventDTO workflowTaskFailAlarmEventDTO);
}
