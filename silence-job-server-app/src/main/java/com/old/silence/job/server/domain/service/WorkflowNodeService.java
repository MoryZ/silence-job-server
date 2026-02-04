package com.old.silence.job.server.domain.service;

import cn.hutool.core.lang.Assert;

import java.math.BigInteger;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.handler.JobHandler;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.core.util.CollectionUtils;


@Service
public class WorkflowNodeService  {

    private final JobTaskBatchDao jobTaskBatchDao;
    private final JobHandler jobHandler;

    public WorkflowNodeService(JobTaskBatchDao jobTaskBatchDao,
                               JobHandler jobHandler) {
        this.jobTaskBatchDao = jobTaskBatchDao;
        this.jobHandler = jobHandler;
    }

    @Transactional
    public Boolean stop(BigInteger nodeId, BigInteger workflowTaskBatchId) {
        // 调用JOB的停止接口
        List<JobTaskBatch> jobTaskBatches = jobTaskBatchDao.selectList(
                new LambdaQueryWrapper<JobTaskBatch>()
                        .eq(JobTaskBatch::getWorkflowNodeId, nodeId)
                        .eq(JobTaskBatch::getWorkflowTaskBatchId, workflowTaskBatchId)
                        .in(JobTaskBatch::getTaskBatchStatus, JobTaskBatchStatus.NOT_COMPLETE)
        );

        if (CollectionUtils.isEmpty(jobTaskBatches)) {
            return Boolean.TRUE;
        }

        for (JobTaskBatch jobTaskBatch : jobTaskBatches) {
            jobHandler.stop(jobTaskBatch.getId());
        }

        return Boolean.TRUE;
    }

    @Transactional
    public Boolean retry(BigInteger nodeId, BigInteger workflowTaskBatchId) {

        // 调用JOB的停止接口
        List<JobTaskBatch> jobTaskBatches = jobTaskBatchDao.selectList(
                new LambdaQueryWrapper<JobTaskBatch>()
                        .select(JobTaskBatch::getId)
                        .eq(JobTaskBatch::getWorkflowNodeId, nodeId)
                        .eq(JobTaskBatch::getWorkflowTaskBatchId, workflowTaskBatchId)
                        .in(JobTaskBatch::getTaskBatchStatus, JobTaskBatchStatus.NOT_SUCCESS)
        );

        Assert.notEmpty(jobTaskBatches, () -> new SilenceJobServerException("job task batch is empty."));

        for (JobTaskBatch jobTaskBatch : jobTaskBatches) {
            jobHandler.retry(jobTaskBatch.getId(), nodeId, workflowTaskBatchId);
        }

        return Boolean.TRUE;
    }
}
