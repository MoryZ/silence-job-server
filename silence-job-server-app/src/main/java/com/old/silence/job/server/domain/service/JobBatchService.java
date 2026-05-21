package com.old.silence.job.server.domain.service;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.server.api.assembler.JobBatchResponseVOConverter;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.domain.model.WorkflowNode;
import com.old.silence.job.server.dto.CallbackConfig;
import com.old.silence.job.server.dto.DecisionConfig;
import com.old.silence.job.server.handler.JobHandler;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowNodeDao;
import com.old.silence.job.server.vo.JobBatchResponseDO;
import com.old.silence.job.server.vo.JobTaskBatchResponseVO;
import com.old.silence.page.PageImpl;


@Service
public class JobBatchService {

    private final JobTaskBatchDao jobTaskBatchDao;
    private final JobDao jobDao;
    private final WorkflowNodeDao workflowNodeDao;
    private final JobHandler jobHandler;
    private final JobBatchResponseVOConverter jobBatchResponseVOConverter;

    public JobBatchService(JobTaskBatchDao jobTaskBatchDao, JobDao jobDao,
                           WorkflowNodeDao workflowNodeDao, JobHandler jobHandler,
                           JobBatchResponseVOConverter jobBatchResponseVOConverter) {
        this.jobTaskBatchDao = jobTaskBatchDao;
        this.jobDao = jobDao;
        this.workflowNodeDao = workflowNodeDao;
        this.jobHandler = jobHandler;
        this.jobBatchResponseVOConverter = jobBatchResponseVOConverter;
    }


    public IPage<JobTaskBatchResponseVO> queryPage(Page<JobTaskBatch> page, QueryWrapper<JobTaskBatch> queryWrapper) {
        return jobTaskBatchDao.selectPage(page, queryWrapper).convert(jobBatchResponseVOConverter::convert);
    }


    public JobTaskBatchResponseVO getJobBatchDetail(BigInteger id) {
        JobTaskBatch jobTaskBatch = jobTaskBatchDao.selectById(id);
        if (Objects.isNull(jobTaskBatch)) {
            return null;
        }

        Job job = jobDao.selectById(jobTaskBatch.getJobId());
        JobTaskBatchResponseVO jobTaskBatchResponseVO = jobBatchResponseVOConverter.convert(jobTaskBatch, job);

        if (jobTaskBatch.getSystemTaskType().equals(SystemTaskType.WORKFLOW)) {
            WorkflowNode workflowNode = workflowNodeDao.selectById(jobTaskBatch.getWorkflowNodeId());
            jobTaskBatchResponseVO.setNodeName(workflowNode.getNodeName());

            // 回调节点
            if (SystemConstants.CALLBACK_JOB_ID.equals(jobTaskBatch.getJobId())) {
                jobTaskBatchResponseVO.setCallback(JSON.parseObject(workflowNode.getNodeInfo(), CallbackConfig.class));
                jobTaskBatchResponseVO.setExecutionAt(jobTaskBatch.getCreatedDate());
                return jobTaskBatchResponseVO;
            }

            // 条件节点
            if (SystemConstants.DECISION_JOB_ID.equals(jobTaskBatch.getJobId())) {
                jobTaskBatchResponseVO.setDecision(JSON.parseObject(workflowNode.getNodeInfo(), DecisionConfig.class));
                jobTaskBatchResponseVO.setExecutionAt(jobTaskBatch.getCreatedDate());
                return jobTaskBatchResponseVO;
            }
        }

        return jobTaskBatchResponseVO;
    }


    public boolean stop(BigInteger taskBatchId) {
        return jobHandler.stop(taskBatchId);
    }


    @Transactional
    public Boolean retry(BigInteger taskBatchId) {
        return jobHandler.retry(taskBatchId);
    }


    public Boolean deleteJobBatchByIds(Set<BigInteger> ids) {
        jobHandler.deleteJobTaskBatchByIds(ids);
        return Boolean.TRUE;
    }
}
