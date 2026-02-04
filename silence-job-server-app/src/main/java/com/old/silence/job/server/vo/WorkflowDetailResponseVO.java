package com.old.silence.job.server.vo;


import com.old.silence.job.common.enums.FailStrategy;
import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.common.enums.TriggerType;
import com.old.silence.job.common.enums.WorkflowNodeType;
import com.old.silence.job.server.dto.CallbackConfig;
import com.old.silence.job.server.dto.DecisionConfig;
import com.old.silence.job.server.dto.JobTaskConfig;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;



public class WorkflowDetailResponseVO {

    /**
     * 工作流ID
     */
    private BigInteger id;

    /**
     * 组名称
     */
    private String workflowName;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 触发类型
     */
    private TriggerType triggerType;

    /**
     * 阻塞策略
     */
    private JobBlockStrategy blockStrategy;

    /**
     * 触发间隔
     */
    private String triggerInterval;

    /**
     * 超时时间
     */
    private Integer executorTimeout;

    /**
     * 0、关闭、1、开启
     */
    private Boolean workflowStatus;

    /**
     * see: {@link JobTaskBatchStatus}
     */
    private JobTaskBatchStatus workflowBatchStatus;

    /**
     * 工作流上下文
     */
    private String wfContext;

    /**
     * DAG节点配置
     */
    private NodeConfig nodeConfig;


    public static class NodeConfig {

        /**
         * 1、任务节点 2、条件节点 3、回调节点
         */
        private WorkflowNodeType nodeType;

        /**
         * 节点信息
         */
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
         * 节点ID
         */
        private BigInteger id;

        /**
         * 1、任务节点 2、条件节点 3、回调节点
         */
        private WorkflowNodeType nodeType;

        /**
         * 节点名称
         */
        private String nodeName;

        /**
         * 优先级
         */
        private Integer priorityLevel;

        /**
         * 工作流状态  0、关闭、1、开启
         */
        private Boolean workflowNodeStatus;

        /**
         * 失败策略 1、跳过 2、阻塞
         */
        private FailStrategy failStrategy;

        /**
         * 任务批次状态
         */
        private JobTaskBatchStatus taskBatchStatus;

        /**
         * 判定配置
         */
        private DecisionConfig decision;

        /**
         * 回调配置
         */
        private CallbackConfig callback;

        /**
         * 任务配置
         */
        private JobTaskConfig jobTask;

        /**
         * 定时任务批次信息
         */
        private List<JobBatchResponseVO> jobBatchList;

        /**
         * 子节点
         */
        private NodeConfig childNode;

        public BigInteger getId() {
            return id;
        }

        public void setId(BigInteger id) {
            this.id = id;
        }

        public WorkflowNodeType getNodeType() {
            return nodeType;
        }

        public void setNodeType(WorkflowNodeType nodeType) {
            this.nodeType = nodeType;
        }

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public Integer getPriorityLevel() {
            return priorityLevel;
        }

        public void setPriorityLevel(Integer priorityLevel) {
            this.priorityLevel = priorityLevel;
        }

        public Boolean getWorkflowNodeStatus() {
            return workflowNodeStatus;
        }

        public void setWorkflowNodeStatus(Boolean workflowNodeStatus) {
            this.workflowNodeStatus = workflowNodeStatus;
        }

        public FailStrategy getFailStrategy() {
            return failStrategy;
        }

        public void setFailStrategy(FailStrategy failStrategy) {
            this.failStrategy = failStrategy;
        }

        public JobTaskBatchStatus getTaskBatchStatus() {
            return taskBatchStatus;
        }

        public void setTaskBatchStatus(JobTaskBatchStatus taskBatchStatus) {
            this.taskBatchStatus = taskBatchStatus;
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

        public JobTaskConfig getJobTask() {
            return jobTask;
        }

        public void setJobTask(JobTaskConfig jobTask) {
            this.jobTask = jobTask;
        }

        public List<JobBatchResponseVO> getJobBatchList() {
            return jobBatchList;
        }

        public void setJobBatchList(List<JobBatchResponseVO> jobBatchList) {
            this.jobBatchList = jobBatchList;
        }

        public NodeConfig getChildNode() {
            return childNode;
        }

        public void setChildNode(NodeConfig childNode) {
            this.childNode = childNode;
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

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public JobBlockStrategy getBlockStrategy() {
        return blockStrategy;
    }

    public void setBlockStrategy(JobBlockStrategy blockStrategy) {
        this.blockStrategy = blockStrategy;
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

    public Boolean getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(Boolean workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public JobTaskBatchStatus getWorkflowBatchStatus() {
        return workflowBatchStatus;
    }

    public void setWorkflowBatchStatus(JobTaskBatchStatus workflowBatchStatus) {
        this.workflowBatchStatus = workflowBatchStatus;
    }

    public String getWfContext() {
        return wfContext;
    }

    public void setWfContext(String wfContext) {
        this.wfContext = wfContext;
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
