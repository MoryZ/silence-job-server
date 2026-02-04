package com.old.silence.job.server.handler;

import cn.hutool.core.lang.Assert;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.graph.MutableGraph;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.enums.WorkflowNodeType;
import com.old.silence.job.server.api.assembler.WorkflowMapper;
import com.old.silence.job.server.domain.model.WorkflowNode;
import com.old.silence.job.server.dto.CallbackConfig;
import com.old.silence.job.server.dto.DecisionConfig;
import com.old.silence.job.server.dto.JobTaskConfig;
import com.old.silence.job.server.dto.WorkflowCommand;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowNodeDao;
import com.old.silence.job.server.vo.WorkflowDetailResponseVO;


@Component("webWorkflowHandler")
public class WorkflowHandler {

    private final WorkflowNodeDao workflowNodeDao;
    private final WorkflowMapper workflowMapper;

    public WorkflowHandler(WorkflowNodeDao workflowNodeDao, WorkflowMapper workflowMapper) {
        this.workflowNodeDao = workflowNodeDao;
        this.workflowMapper = workflowMapper;
    }

    /**
     * 根据给定的图、父节点ID、节点配置Map和工作流节点Map，构建节点配置
     *
     * @param graph           图
     * @param parentId        父节点ID
     * @param nodeConfigMap   节点配置Map
     * @param workflowNodeMap 工作流节点Map
     * @return 构建的节点配置
     */
    public WorkflowDetailResponseVO.NodeConfig buildNodeConfig(MutableGraph<BigInteger> graph,
                                                               BigInteger parentId,
                                                               Map<BigInteger, WorkflowDetailResponseVO.NodeConfig> nodeConfigMap,
                                                               Map<BigInteger, WorkflowDetailResponseVO.NodeInfo> workflowNodeMap) {

        Set<BigInteger> successors = graph.successors(parentId);
        if (CollectionUtils.isEmpty(successors)) {
            return null;
        }

        WorkflowDetailResponseVO.NodeInfo previousNodeInfo = workflowNodeMap.get(parentId);
        WorkflowDetailResponseVO.NodeConfig currentConfig = new WorkflowDetailResponseVO.NodeConfig();
        currentConfig.setConditionNodes(Lists.newArrayList());

        // 是否挂载子节点
        boolean mount = false;

        for (BigInteger successor : Sets.newTreeSet(successors)) {
            Set<BigInteger> predecessors = graph.predecessors(successor);
            WorkflowDetailResponseVO.NodeInfo nodeInfo = workflowNodeMap.get(successor);
            currentConfig.setNodeType(nodeInfo.getNodeType());
            currentConfig.getConditionNodes().add(nodeInfo);
            nodeConfigMap.put(successor, currentConfig);

            if (predecessors.size() >= 2) {
                // 查找predecessors的公共祖先节点
                Map<BigInteger, Set<BigInteger>> sets = new HashMap<>();
                for (BigInteger predecessor : predecessors) {
                    Set<BigInteger> set = Sets.newTreeSet();
                    sets.put(predecessor, set);
                    findCommonAncestor(predecessor, set, graph);
                }

                Set<BigInteger> intersection = sets.values().stream().findFirst().get();
                for (Set<BigInteger> value : sets.values()) {
                    intersection = Sets.intersection(value, intersection);
                }

                BigInteger commonAncestor = intersection.stream().collect(Collectors.toList()).get(intersection.size() - 1);
                WorkflowDetailResponseVO.NodeConfig parentNodeConfig = nodeConfigMap.get(
                        Sets.newTreeSet(graph.successors(commonAncestor)).stream().findFirst().get());
                parentNodeConfig.setChildNode(currentConfig);
                mount = false;
            } else {
                mount = true;
            }

            buildNodeConfig(graph, successor, nodeConfigMap, workflowNodeMap);
        }

        if (!parentId.equals(SystemConstants.ROOT) && mount) {
            previousNodeInfo.setChildNode(currentConfig);
        }

        currentConfig.getConditionNodes().sort(Comparator.comparing(WorkflowDetailResponseVO.NodeInfo::getPriorityLevel));
        return currentConfig;
    }

    private void findCommonAncestor(BigInteger predecessor, Set<BigInteger> set, MutableGraph<BigInteger> graph) {

        Set<BigInteger> predecessors = graph.predecessors(predecessor);
        if (CollectionUtils.isEmpty(predecessors)) {
            return;
        }

        set.addAll(predecessors);

        findCommonAncestor(new ArrayList<>(predecessors).get(0), set, graph);
    }

    /**
     * 根据给定的父节点ID、队列、工作流组名、工作流ID、节点配置、图构建图
     *
     * @param parentIds  父节点ID列表
     * @param deque      队列
     * @param groupName  工作流组名
     * @param workflowId 工作流ID
     * @param nodeConfig 节点配置
     * @param graph      图
     * @param version    版本号
     */
    public void buildGraph(List<BigInteger> parentIds, LinkedBlockingDeque<BigInteger> deque, String groupName, BigInteger workflowId,
                           WorkflowCommand.NodeConfig nodeConfig, MutableGraph<BigInteger> graph, Integer version) {

        if (Objects.isNull(nodeConfig)) {
            return;
        }

        LinkedBlockingDeque<BigInteger> tempDeque = null;
        // 获取节点信息
        List<WorkflowCommand.NodeInfo> conditionNodes = nodeConfig.getConditionNodes();
        if (CollectionUtils.isNotEmpty(conditionNodes)) {
            // 一定存在汇合的子节点
            if (Objects.nonNull(nodeConfig.getChildNode())) {
                tempDeque = new LinkedBlockingDeque<>();
            }
            conditionNodes = conditionNodes.stream()
                    .sorted(Comparator.comparing(WorkflowCommand.NodeInfo::getPriorityLevel))
                    .collect(Collectors.toList());
            for (WorkflowCommand.NodeInfo nodeInfo : conditionNodes) {
                WorkflowNode workflowNode = workflowMapper.convert(nodeInfo);
                workflowNode.setWorkflowId(workflowId);
                workflowNode.setGroupName(groupName);
                workflowNode.setNodeType(nodeConfig.getNodeType());
                workflowNode.setVersion(version);
                if (WorkflowNodeType.DECISION == nodeConfig.getNodeType()) {
                    workflowNode.setJobId(SystemConstants.DECISION_JOB_ID);
                    DecisionConfig decision = nodeInfo.getDecision();
                    Assert.notNull(decision, () -> new SilenceJobServerException("【{}】配置信息不能为空", nodeInfo.getNodeName()));
                    Assert.notBlank(decision.getNodeExpression(), () -> new SilenceJobServerException("【{}】表达式不能为空", nodeInfo.getNodeName()));
                    Assert.notNull(decision.getDefaultDecision(), () -> new SilenceJobServerException("【{}】默认决策不能为空", nodeInfo.getNodeName()));
                    Assert.notNull(decision.getExpressionType(), () -> new SilenceJobServerException("【{}】表达式类型不能为空", nodeInfo.getNodeName()));
                    workflowNode.setNodeInfo(JSON.toJSONString(decision));
                }

                if (WorkflowNodeType.CALLBACK == nodeConfig.getNodeType()) {
                    workflowNode.setJobId(SystemConstants.CALLBACK_JOB_ID);
                    CallbackConfig callback = nodeInfo.getCallback();
                    Assert.notNull(callback, () -> new SilenceJobServerException("【{}】配置信息不能为空", nodeInfo.getNodeName()));
                    Assert.notBlank(callback.getWebhook(), () -> new SilenceJobServerException("【{}】webhook不能为空", nodeInfo.getNodeName()));
                    Assert.notNull(callback.getContentType(), () -> new SilenceJobServerException("【{}】请求类型不能为空", nodeInfo.getNodeName()));
                    Assert.notBlank(callback.getSecret(), () -> new SilenceJobServerException("【{}】秘钥不能为空", nodeInfo.getNodeName()));
                    workflowNode.setNodeInfo(JSON.toJSONString(callback));
                }

                if (WorkflowNodeType.JOB_TASK == nodeConfig.getNodeType()) {
                    JobTaskConfig jobTask = nodeInfo.getJobTask();
                    Assert.notNull(jobTask, () -> new SilenceJobServerException("【{}】配置信息不能为空", nodeInfo.getNodeName()));
                    Assert.notNull(jobTask.getJobId(), () -> new SilenceJobServerException("【{}】所属任务不能为空", nodeInfo.getNodeName()));
                    workflowNode.setJobId(jobTask.getJobId());
                }

                Assert.isTrue(1 == workflowNodeDao.insert(workflowNode),
                        () -> new SilenceJobServerException("新增工作流节点失败"));
                // 添加节点
                graph.addNode(workflowNode.getId());
                for (BigInteger parentId : parentIds) {
                    // 添加边
                    graph.putEdge(parentId, workflowNode.getId());
                }
                WorkflowCommand.NodeConfig childNode = nodeInfo.getChildNode();
                if (Objects.nonNull(childNode) && CollectionUtils.isNotEmpty(childNode.getConditionNodes())) {
                    buildGraph(Lists.newArrayList(workflowNode.getId()),
                            Objects.isNull(tempDeque) ? deque : tempDeque,
                            groupName, workflowId, childNode, graph, version);
                } else {
                    if (WorkflowNodeType.DECISION == nodeConfig.getNodeType()) {
                        throw new SilenceJobServerException("决策节点或者决策节点的后继节点不能作为叶子节点");
                    }

                    // 若当前节点无子任何子节点记录一下, 后续存在公共子节点时需要用到
                    if (Objects.nonNull(tempDeque)) {
                        tempDeque.add(workflowNode.getId());
                    } else {
                        // 当前节点无汇合的子节点放到公共的队列
                        deque.add(workflowNode.getId());
                    }

                }
            }
        }

        WorkflowCommand.NodeConfig childNode = nodeConfig.getChildNode();
        // 如果存在公共子节点则在这里处理
        if (Objects.nonNull(childNode) && CollectionUtils.isNotEmpty(childNode.getConditionNodes())) {
            //  是conditionNodes里面叶子节点的
            List<BigInteger> list = Lists.newArrayList();
            if (Objects.nonNull(tempDeque)) {
                tempDeque.drainTo(list);
            }

            buildGraph(list, deque, groupName, workflowId, childNode, graph, version);
        }
    }


}
