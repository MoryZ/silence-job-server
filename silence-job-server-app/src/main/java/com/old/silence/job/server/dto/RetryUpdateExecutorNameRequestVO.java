package com.old.silence.job.server.dto;

import com.old.silence.job.common.enums.RetryStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.math.BigInteger;
import java.util.List;

/**
 * 更新执行器名称
 *
 */

public class RetryUpdateExecutorNameRequestVO {

    /**
     * 组名称
     */
    @NotBlank(message = "组名称不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母、下划线和短横线")
    private String groupName;

    /**
     * 执行器名称
     */
    private String executorName;

    /**
     * 重试状态 {@link RetryStatus}
     */
    private RetryStatus retryStatus;

    /**
     * 重试表id
     */
    @NotEmpty(message = "至少选择一项")
    private List<BigInteger> ids;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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

    public List<BigInteger> getIds() {
        return ids;
    }

    public void setIds(List<BigInteger> ids) {
        this.ids = ids;
    }
}
