package com.old.silence.job.server.job.task.dto;


public class MapReduceArgsStrDTO {

    private Integer shardNum;

    private String argsStr;

    public Integer getShardNum() {
        return shardNum;
    }

    public void setShardNum(Integer shardNum) {
        this.shardNum = shardNum;
    }

    public String getArgsStr() {
        return argsStr;
    }

    public void setArgsStr(String argsStr) {
        this.argsStr = argsStr;
    }
}
