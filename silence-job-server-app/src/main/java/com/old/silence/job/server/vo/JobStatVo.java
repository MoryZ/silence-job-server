package com.old.silence.job.server.vo;



/**
 * @author moryzang
 */

public class JobStatVo {
    private Integer localJobCount;
    private Integer remoteJobCount;

    public Integer getLocalJobCount() {
        return localJobCount;
    }

    public void setLocalJobCount(Integer localJobCount) {
        this.localJobCount = localJobCount;
    }

    public Integer getRemoteJobCount() {
        return remoteJobCount;
    }

    public void setRemoteJobCount(Integer remoteJobCount) {
        this.remoteJobCount = remoteJobCount;
    }
}
