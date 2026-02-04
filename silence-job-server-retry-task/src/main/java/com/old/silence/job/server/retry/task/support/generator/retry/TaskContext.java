package com.old.silence.job.server.retry.task.support.generator.retry;



import com.old.silence.job.common.enums.RetryStatus;

import java.util.List;

/**
 * 任务生成器上下文
 *
 */

public class TaskContext {

    /**
     * namespaceId
     */
    private String namespaceId;

    /**
     * groupName
     */
    private String groupName;

    /**
     * sceneName
     */
    private String sceneName;

    /**
     * 任务的初始状态
     */
    private RetryStatus initStatus;

    /**
     * 任务信息
     */
    private List<TaskInfo> taskInfos;

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public RetryStatus getInitStatus() {
        return initStatus;
    }

    public void setInitStatus(RetryStatus initStatus) {
        this.initStatus = initStatus;
    }

    public List<TaskInfo> getTaskInfos() {
        return taskInfos;
    }

    public void setTaskInfos(List<TaskInfo> taskInfos) {
        this.taskInfos = taskInfos;
    }

    public static class TaskInfo {
        /**
         * 业务唯一id
         */
        private String idempotentId;

        /**
         * 执行器名称
         */
        private String executorName;

        /**
         * 业务唯一编号
         */
        private String bizNo;

        /**
         * 客户端上报参数
         */
        private String argsStr;

        /**
         * 额外扩展参数
         */
        private String extAttrs;

        public String getIdempotentId() {
            return idempotentId;
        }

        public void setIdempotentId(String idempotentId) {
            this.idempotentId = idempotentId;
        }

        public String getExecutorName() {
            return executorName;
        }

        public void setExecutorName(String executorName) {
            this.executorName = executorName;
        }

        public String getBizNo() {
            return bizNo;
        }

        public void setBizNo(String bizNo) {
            this.bizNo = bizNo;
        }

        public String getArgsStr() {
            return argsStr;
        }

        public void setArgsStr(String argsStr) {
            this.argsStr = argsStr;
        }

        public String getExtAttrs() {
            return extAttrs;
        }

        public void setExtAttrs(String extAttrs) {
            this.extAttrs = extAttrs;
        }
    }
}
