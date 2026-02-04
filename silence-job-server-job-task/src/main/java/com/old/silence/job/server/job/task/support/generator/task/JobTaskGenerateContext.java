package com.old.silence.job.server.job.task.support.generator.task;


import com.old.silence.job.common.enums.JobArgsType;
import com.old.silence.job.common.enums.MapReduceStage;

import java.math.BigInteger;
import java.util.List;


public class JobTaskGenerateContext {
    /**
     * 命名空间id
     */
    private String namespaceId;

    private BigInteger taskBatchId;
    private String groupName;
    private BigInteger jobId;
    private Integer routeKey;
    /**
     * 执行方法参数
     */
    private String argsStr;

    /**
     * 参数类型 text/json
     */
    private JobArgsType argsType;

    /**
     * 动态分片的Map任务
     */
    private List<?> mapSubTask;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 动态分片的阶段
     */
    private MapReduceStage mrStage;

    /**
     * 父任务id
     */
    private BigInteger parentId;


    private String wfContext;


    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public BigInteger getTaskBatchId() {
        return taskBatchId;
    }

    public void setTaskBatchId(BigInteger taskBatchId) {
        this.taskBatchId = taskBatchId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public BigInteger getJobId() {
        return jobId;
    }

    public void setJobId(BigInteger jobId) {
        this.jobId = jobId;
    }

    public Integer getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(Integer routeKey) {
        this.routeKey = routeKey;
    }

    public String getArgsStr() {
        return argsStr;
    }

    public void setArgsStr(String argsStr) {
        this.argsStr = argsStr;
    }

    public JobArgsType getArgsType() {
        return argsType;
    }

    public void setArgsType(JobArgsType argsType) {
        this.argsType = argsType;
    }

    public List<?> getMapSubTask() {
        return mapSubTask;
    }

    public void setMapSubTask(List<?> mapSubTask) {
        this.mapSubTask = mapSubTask;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public MapReduceStage getMrStage() {
        return mrStage;
    }

    public void setMrStage(MapReduceStage mrStage) {
        this.mrStage = mrStage;
    }

    public BigInteger getParentId() {
        return parentId;
    }

    public void setParentId(BigInteger parentId) {
        this.parentId = parentId;
    }

    public String getWfContext() {
        return wfContext;
    }

    public void setWfContext(String wfContext) {
        this.wfContext = wfContext;
    }
}
