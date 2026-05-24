package com.old.silence.job.server.api.assembler;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import com.alibaba.fastjson2.JSON;
import com.old.silence.core.mapstruct.MapStructSpringConfig;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.WorkflowNodeType;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.domain.model.Workflow;
import com.old.silence.job.server.domain.model.WorkflowNotifyConfigRelation;
import com.old.silence.job.server.domain.model.WorkflowNode;
import com.old.silence.job.server.domain.model.WorkflowTaskBatch;
import com.old.silence.job.server.dto.CallbackConfig;
import com.old.silence.job.server.dto.DecisionConfig;
import com.old.silence.job.server.dto.JobTaskConfig;
import com.old.silence.job.server.dto.WorkflowCommand;
import com.old.silence.job.server.vo.WorkflowBatchResponseDO;
import com.old.silence.job.server.vo.WorkflowBatchResponseVO;
import com.old.silence.job.server.vo.WorkflowDetailResponseVO;
import com.old.silence.job.server.vo.WorkflowNotifyConfigRelationView;
import com.old.silence.job.server.vo.WorkflowResponseVO;
import com.old.silence.job.server.vo.WorkflowView;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring", uses = MapStructSpringConfig.class)
public interface WorkflowMapper extends Converter<WorkflowCommand, Workflow> {


    @Override
    @Mapping(target = "notifyRelations", expression = "java(toWorkflowNotifyConfigRelations(workflowCommand.getNotifyIds()))")
    Workflow convert(WorkflowCommand workflowCommand);

    WorkflowNode convert(WorkflowCommand.NodeInfo nodeInfo);

    @Mapping(target = "notifyIds", expression = "java(toNotifyIds(workflow.getNotifyRelations()))")
    WorkflowDetailResponseVO convert(WorkflowView workflow);

    @Mapping(target = "decision", expression = "java(parseDecisionConfig(workflowNode))")
    @Mapping(target = "callback", expression = "java(parseCallbackConfig(workflowNode))")
    @Mapping(target = "jobTask", expression = "java(parseJobTaskConfig(workflowNode))")
    WorkflowDetailResponseVO.NodeInfo convert(WorkflowNode workflowNode);


    @Mapping(target = "nextTriggerAt", expression = "java(toLocalDateTime(workflow.getNextTriggerAt()))")
    @Mapping(target = "notifyIds", expression = "java(toNotifyIds(workflow.getNotifyRelations()))")
    WorkflowResponseVO convertToWorkflow(WorkflowView workflow);

    @Mapping(source = "workflowTaskBatch.groupName", target = "groupName")
    @Mapping(source = "workflowTaskBatch.id", target = "id")
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "executionAt", expression = "java(toLocalDateTime(workflowTaskBatch.getExecutionAt()))")
    WorkflowBatchResponseVO convert(WorkflowTaskBatch workflowTaskBatch, Workflow workflow);


    WorkflowBatchResponseVO convertWorkflowBatchResponseVO(WorkflowBatchResponseDO workflowBatchResponseDO);

    default Instant toLocalDateTime(Long nextTriggerAt) {
        if (Objects.isNull(nextTriggerAt) || nextTriggerAt == 0) {
            return null;
        }

        return DateUtils.toLocalDateTime(nextTriggerAt);
    }

    default DecisionConfig parseDecisionConfig(WorkflowNode workflowNode) {
        if (WorkflowNodeType.DECISION == workflowNode.getNodeType()) {
            return JSON.parseObject(workflowNode.getNodeInfo(), DecisionConfig.class);
        }

        return null;
    }

    default CallbackConfig parseCallbackConfig(WorkflowNode workflowNode) {
        if (WorkflowNodeType.CALLBACK == workflowNode.getNodeType()) {
            return JSON.parseObject(workflowNode.getNodeInfo(), CallbackConfig.class);
        }

        return null;
    }

    default JobTaskConfig parseJobTaskConfig(WorkflowNode workflowNode) {
        if (WorkflowNodeType.JOB_TASK == workflowNode.getNodeType()) {
            JobTaskConfig jobTaskConfig = new JobTaskConfig();
            jobTaskConfig.setJobId(workflowNode.getJobId());
            return jobTaskConfig;
        }

        return null;
    }

    default Set<BigInteger> toNotifyIds(List<WorkflowNotifyConfigRelationView> notifyRelations) {
        return CollectionUtils.transformToSet(notifyRelations, WorkflowNotifyConfigRelationView::getNotifyConfigId);
    }

    default List<WorkflowNotifyConfigRelation> toWorkflowNotifyConfigRelations(Set<BigInteger> notifyIds) {
        if (CollectionUtils.isEmpty(notifyIds)) {
            return List.of();
        }

        return notifyIds.stream().map(notifyId -> {
            var workflowNotifyConfigRelation = new WorkflowNotifyConfigRelation();
            workflowNotifyConfigRelation.setNotifyConfigId(notifyId);
            return workflowNotifyConfigRelation;
        }).collect(Collectors.toList());
    }

}
