package com.old.silence.job.server.dto;


import com.old.silence.job.common.enums.SystemModeEnum;

public class JobLineQueryVo extends LineQueryVO {
    /**
     * 系统模式
     *
     * @see SystemModeEnum
     */
    private SystemModeEnum mode;

    public SystemModeEnum getMode() {
        return mode;
    }

    public void setMode(SystemModeEnum mode) {
        this.mode = mode;
    }
}
