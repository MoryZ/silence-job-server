package com.old.silence.job.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.math.BigInteger;
import java.util.List;



public class ManualTriggerTaskRequestVO {

    @NotBlank(message = "groupName 不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母、下划线和短横线")
    private String groupName;

    @NotEmpty(message = "retryIds 不能为空")
    private List<BigInteger> retryIds;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<BigInteger> getRetryIds() {
        return retryIds;
    }

    public void setRetryIds(List<BigInteger> retryIds) {
        this.retryIds = retryIds;
    }
}
