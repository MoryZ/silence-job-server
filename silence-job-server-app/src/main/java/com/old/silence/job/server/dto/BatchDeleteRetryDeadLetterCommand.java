package com.old.silence.job.server.dto;




import jakarta.validation.constraints.NotEmpty;
import java.math.BigInteger;
import java.util.List;

/**
 * 批量删除死信表数据
 *
 */

public class BatchDeleteRetryDeadLetterCommand {

    /**
     * 重试表id
     */
    @NotEmpty(message = "至少选择一项")
    private List<BigInteger> ids;

    public List<BigInteger> getIds() {
        return ids;
    }

    public void setIds(List<BigInteger> ids) {
        this.ids = ids;
    }
}
