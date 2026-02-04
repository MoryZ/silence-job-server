package com.old.silence.job.server.dto;

import com.old.silence.job.common.enums.FailStrategy;
import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.common.enums.TriggerType;
import com.old.silence.job.common.enums.WorkflowNodeType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;



public class WorkflowCommand {

    private BigInteger id;

    @NotBlank(message = "组名称不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母、下划线和短横线")
    private String groupName;

    @NotBlank(message = "工作流名称不能为空")
    private String workflowName;

    @NotNull(message = "触发类型不能为空")
    private TriggerType triggerType;

    @NotBlank(message = "触发间隔不能为空")
    private String triggerInterval;

    @NotNull(message = "执行超时时间不能为空")
    private Integer executorTimeout;

    @NotNull(message = "阻塞策略不能为空")
    private JobBlockStrategy blockStrategy;

    /**
     * 工作流上下文
     */
    private String wfContext;

    /**
     * 0、关闭、1、开启
     */
    @NotNull(message = "工作流状态")
    private Boolean workflowStatus;

    /**
     * 描述
     */
    private String description;

    /**
     * DAG节点配置
     */
    @NotNull(message = "DAG节点配置不能为空")
    private NodeConfig nodeConfig;


    public static class NodeConfig {

        /**
         * 1、任务节点 2、条件节点
         */
        @NotNull(message = "节点类型不能为空 ")
        private WorkflowNodeType nodeType;

        /**
         * 节点信息
         */
        @NotEmpty(message = "节点信息不能为空")
        private List<NodeInfo> conditionNodes;

        /**
         * 子节点
         */
        private NodeConfig childNode;

        public WorkflowNodeType getNodeType() {
            return nodeType;
        }

        public void setNodeType(WorkflowNodeType nodeType) {
            this.nodeType = nodeType;
        }

        public List<NodeInfo> getConditionNodes() {
            return conditionNodes;
        }

        public void setConditionNodes(List<NodeInfo> conditionNodes) {
            this.conditionNodes = conditionNodes;
        }

        public NodeConfig getChildNode() {
            return childNode;
        }

        public void setChildNode(NodeConfig childNode) {
            this.childNode = childNode;
        }
    }


    public static class NodeInfo {

        /**
         * 节点名称
         */
        @NotBlank(message = "节点名称不能为空")
        private String nodeName;

        /**
         * 工作流状态  0、关闭、1、开启
         */
        @NotNull(message = "工作流状态不能为空")
        private Boolean workflowNodeStatus;

        /**
         * 优先级
         */
        @NotNull(message = "优先级不能为空")
        private Integer priorityLevel;

        /**
         * 子节点
         */
        private NodeConfig childNode;

        /**
         * 1、跳过 2、阻塞
         */
        private FailStrategy failStrategy;

        /**
         * 任务节点配置
         */
        private JobTaskConfig jobTask;

        /**
         * 决策节点配置
         */
        private DecisionConfig decision;

        /**
         * 回调配置
         */
        private CallbackConfig callback;

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public Boolean getWorkflowNodeStatus() {
            return workflowNodeStatus;
        }

        public void setWorkflowNodeStatus(Boolean workflowNodeStatus) {
            this.workflowNodeStatus = workflowNodeStatus;
        }

        public Integer getPriorityLevel() {
            return priorityLevel;
        }

        public void setPriorityLevel(Integer priorityLevel) {
            this.priorityLevel = priorityLevel;
        }

        public NodeConfig getChildNode() {
            return childNode;
        }

        public void setChildNode(NodeConfig childNode) {
            this.childNode = childNode;
        }

        public FailStrategy getFailStrategy() {
            return failStrategy;
        }

        public void setFailStrategy(FailStrategy failStrategy) {
            this.failStrategy = failStrategy;
        }

        public JobTaskConfig getJobTask() {
            return jobTask;
        }

        public void setJobTask(JobTaskConfig jobTask) {
            this.jobTask = jobTask;
        }

        public DecisionConfig getDecision() {
            return decision;
        }

        public void setDecision(DecisionConfig decision) {
            this.decision = decision;
        }

        public CallbackConfig getCallback() {
            return callback;
        }

        public void setCallback(CallbackConfig callback) {
            this.callback = callback;
        }
    }

    /**
     * 通知告警场景配置id列表
     */
    private Set<BigInteger> notifyIds;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggerInterval() {
        return triggerInterval;
    }

    public void setTriggerInterval(String triggerInterval) {
        this.triggerInterval = triggerInterval;
    }

    public Integer getExecutorTimeout() {
        return executorTimeout;
    }

    public void setExecutorTimeout(Integer executorTimeout) {
        this.executorTimeout = executorTimeout;
    }

    public JobBlockStrategy getBlockStrategy() {
        return blockStrategy;
    }

    public void setBlockStrategy(JobBlockStrategy blockStrategy) {
        this.blockStrategy = blockStrategy;
    }

    public String getWfContext() {
        return wfContext;
    }

    public void setWfContext(String wfContext) {
        this.wfContext = wfContext;
    }

    public Boolean getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(Boolean workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NodeConfig getNodeConfig() {
        return nodeConfig;
    }

    public void setNodeConfig(NodeConfig nodeConfig) {
        this.nodeConfig = nodeConfig;
    }

    public Set<BigInteger> getNotifyIds() {
        return notifyIds;
    }

    public void setNotifyIds(Set<BigInteger> notifyIds) {
        this.notifyIds = notifyIds;
    }
}
