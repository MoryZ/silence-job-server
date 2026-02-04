package com.old.silence.job.server.common.vo;

import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;



public class JobStatusUpdateRequestVO {

    @NotNull(message = "id 不能为空")
    private BigInteger id;

    @NotNull(message = "jobStatus 不能为空")
    private Boolean jobStatus;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public Boolean getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(Boolean jobStatus) {
        this.jobStatus = jobStatus;
    }
}
