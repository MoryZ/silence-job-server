package com.old.silence.job.server.dto;

import com.old.silence.job.common.enums.RetryStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigInteger;


/**
 * 更新重试任务模型
 *
 */

public class RetryUpdateStatusRequestVO {

    /**
     * 重试状态 {@link RetryStatus}
     */
    @NotNull(message = "重试状态 不能为空")
    private RetryStatus retryStatus;

    /**
     * 组名称
     */
    @NotBlank(message = "组名称 不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母、下划线和短横线")
    private String groupName;

    /**
     * 重试表id
     */
    @NotNull(message = "重试表id 不能为空")
    private BigInteger id;

    public RetryStatus getRetryStatus() {
        return retryStatus;
    }

    public void setRetryStatus(RetryStatus retryStatus) {
        this.retryStatus = retryStatus;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }
}
