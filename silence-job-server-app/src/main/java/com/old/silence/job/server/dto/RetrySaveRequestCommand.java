package com.old.silence.job.server.dto;

import com.old.silence.job.common.enums.RetryStatus;

import jakarta.validation.constraints.NotBlank;


/**
 * 重试数据模型
 *
 */

public class RetrySaveRequestCommand {

    /**
     * 组名称
     */
    @NotBlank(message = "组名称不能为空")
    private String groupName;

    /**
     * 场景名称
     */
    @NotBlank(message = "场景名称不能为空")
    private String sceneName;

    /**
     * 幂等id(同一个场景下正在重试中的idempotentId不能重复)
     */
    @NotBlank(message = "幂等id不能为空")
    private String idempotentId;

    /**
     * 业务编号
     */
    private String bizNo;

    /**
     * 重试参数
     */
    private String argsStr;

    /**
     * 扩展重试参数
     */
    private String extAttrs;

    /**
     * 执行器名称(全类路径)
     */
    @NotBlank(message = "执行器名称不能为空")
    private String executorName;

    /**
     * 重试状态 {@link RetryStatus}
     */
    private RetryStatus retryStatus;

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

    public String getIdempotentId() {
        return idempotentId;
    }

    public void setIdempotentId(String idempotentId) {
        this.idempotentId = idempotentId;
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

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }

    public RetryStatus getRetryStatus() {
        return retryStatus;
    }

    public void setRetryStatus(RetryStatus retryStatus) {
        this.retryStatus = retryStatus;
    }
}
