package com.old.silence.job.server.dto;

import com.old.silence.job.common.enums.RetryStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 解析参数模型
 *
 */

public class ParseLogsVO {

    /**
     * 客户端打印的上报日志信息
     */
    @NotBlank(message = "组名称不能为空")
    private String logStr;

    /**
     * 组名称
     */
    @NotBlank(message = "组名称不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母、下划线和短横线")
    private String groupName;

    @NotNull(message = "重试状态不能为空")
    private RetryStatus retryStatus;

    public String getLogStr() {
        return logStr;
    }

    public void setLogStr(String logStr) {
        this.logStr = logStr;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public RetryStatus getRetryStatus() {
        return retryStatus;
    }

    public void setRetryStatus(RetryStatus retryStatus) {
        this.retryStatus = retryStatus;
    }
}
