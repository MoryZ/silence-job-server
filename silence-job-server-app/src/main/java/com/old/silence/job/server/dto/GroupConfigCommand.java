package com.old.silence.job.server.dto;

import com.old.silence.job.common.enums.IdGeneratorMode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


/**
 * 组、场景、通知配置类
 *
 */

public class GroupConfigCommand {

    @NotBlank(message = "组名称不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母、下划线和短横线")
    private String groupName;

    @NotNull(message = "组状态不能为空")
    private Boolean groupStatus;

    /**
     * 令牌
     */
    @NotBlank(message = "令牌不能为空")
    private String token;

    /**
     * 描述
     */
    private String description;

    /**
     * 分区
     */
    @NotNull(message = "分区不能为空")
    private Integer groupPartition;

    /**
     * 唯一id生成模式
     * {@link IdGeneratorMode}
     */
    @NotNull(message = "id生成模式不能为空")
    private IdGeneratorMode idGeneratorMode;

    /**
     * 是否初始化场景
     */
    @NotNull(message = "初始化场不能为空")
    private Boolean initScene;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Boolean getGroupStatus() {
        return groupStatus;
    }

    public void setGroupStatus(Boolean groupStatus) {
        this.groupStatus = groupStatus;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getGroupPartition() {
        return groupPartition;
    }

    public void setGroupPartition(Integer groupPartition) {
        this.groupPartition = groupPartition;
    }

    public IdGeneratorMode getIdGeneratorMode() {
        return idGeneratorMode;
    }

    public void setIdGeneratorMode(IdGeneratorMode idGeneratorMode) {
        this.idGeneratorMode = idGeneratorMode;
    }

    public Boolean getInitScene() {
        return initScene;
    }

    public void setInitScene(Boolean initScene) {
        this.initScene = initScene;
    }
}
