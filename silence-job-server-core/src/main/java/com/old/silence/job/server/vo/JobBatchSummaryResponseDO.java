package com.old.silence.job.server.vo;



import com.old.silence.job.common.enums.JobTaskBatchStatus;

import java.math.BigInteger;

/**
 * stop => 已经向客户端下发了执行任务指令
 * cancel => 未向客户端下发指令
 * fail => 客户端上报执行失败，或者服务端执行失败
 *
 */

public class JobBatchSummaryResponseDO {

    /**
     * 命名空间
     */
    private String namespaceId;

    /**
     * 组名
     */
    private String groupName;

    /**
     * 任务id
     */
    private BigInteger jobId;

    /**
     * 工作流任务id
     */
    //private BigInteger workflowId;

    /**
     * 任务批次状态
     */
    private JobTaskBatchStatus taskBatchStatus;

    /**
     * 操作原因
     */
    private Long operationReason;

    /**
     * 操作原因总数
     */
    private Integer operationReasonTotal;

    /**
     * 执行成功-日志数量
     */
    private Integer successNum;

    /**
     * cancel执行失败-日志数量
     */
    private Integer cancelNum;

    /**
     * stop执行失败-日志数量
     */
    private Integer stopNum;

    /**
     * fail执行失败-日志数量
     */
    private Integer failNum;

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
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

    public JobTaskBatchStatus getTaskBatchStatus() {
        return taskBatchStatus;
    }

    public void setTaskBatchStatus(JobTaskBatchStatus taskBatchStatus) {
        this.taskBatchStatus = taskBatchStatus;
    }

    public Long getOperationReason() {
        return operationReason;
    }

    public void setOperationReason(Long operationReason) {
        this.operationReason = operationReason;
    }

    public Integer getOperationReasonTotal() {
        return operationReasonTotal;
    }

    public void setOperationReasonTotal(Integer operationReasonTotal) {
        this.operationReasonTotal = operationReasonTotal;
    }

    public Integer getSuccessNum() {
        return successNum;
    }

    public void setSuccessNum(Integer successNum) {
        this.successNum = successNum;
    }

    public Integer getCancelNum() {
        return cancelNum;
    }

    public void setCancelNum(Integer cancelNum) {
        this.cancelNum = cancelNum;
    }

    public Integer getStopNum() {
        return stopNum;
    }

    public void setStopNum(Integer stopNum) {
        this.stopNum = stopNum;
    }

    public Integer getFailNum() {
        return failNum;
    }

    public void setFailNum(Integer failNum) {
        this.failNum = failNum;
    }
}
