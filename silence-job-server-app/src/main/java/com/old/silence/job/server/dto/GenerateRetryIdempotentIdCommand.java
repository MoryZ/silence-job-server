package com.old.silence.job.server.dto;



import jakarta.validation.constraints.NotBlank;

/**
 * 生成idempotentId模型
 *
 */

public class GenerateRetryIdempotentIdCommand {
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
     * 执行参数
     */
    @NotBlank(message = "参数不能为空")
    private String argsStr;

    /**
     * 执行器名称
     */
    @NotBlank(message = "执行器不能为空")
    private String executorName;

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

    public String getArgsStr() {
        return argsStr;
    }

    public void setArgsStr(String argsStr) {
        this.argsStr = argsStr;
    }

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }
}
