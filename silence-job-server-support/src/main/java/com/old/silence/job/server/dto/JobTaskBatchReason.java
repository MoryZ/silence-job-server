package com.old.silence.job.server.common.dto;


public class JobTaskBatchReason {

    /**
     * 操作原因
     */
    private Long reason;

    /**
     * 操作原因总数
     */
    private Integer total;

    public Long getReason() {
        return reason;
    }

    public void setReason(Long reason) {
        this.reason = reason;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
