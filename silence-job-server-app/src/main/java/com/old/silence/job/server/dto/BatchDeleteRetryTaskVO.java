package com.old.silence.job.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigInteger;
import java.util.List;

/**
 * 批量删除重试数据
 *
 */

public class BatchDeleteRetryTaskVO {

    /**
     * 组名称
     */
    @NotBlank(message = "groupName 不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母、下划线和短横线")
    private String groupName;

    /**
     * 重试表id
     */
    @NotEmpty(message = "至少选择一项")
    @Size(max = 100, message = "最多只能删除100条")
    private List<BigInteger> ids;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<BigInteger> getIds() {
        return ids;
    }

    public void setIds(List<BigInteger> ids) {
        this.ids = ids;
    }
}
