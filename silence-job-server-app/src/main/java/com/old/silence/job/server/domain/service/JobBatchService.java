package com.old.silence.job.server.domain.service;

import java.math.BigInteger;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.server.api.assembler.JobBatchResponseVOConverter;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.domain.model.WorkflowNode;
import com.old.silence.job.server.dto.CallbackConfig;
import com.old.silence.job.server.dto.DecisionConfig;
import com.old.silence.job.server.handler.JobHandler;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowNodeDao;
import com.old.silence.job.server.vo.JobTaskBatchAndJobView;
import com.old.silence.job.server.vo.JobTaskBatchResponseVO;


@Service
public class JobBatchService {

    private final JobTaskBatchDao jobTaskBatchDao;
    private final WorkflowNodeDao workflowNodeDao;
    private final JobHandler jobHandler;
    private final JobBatchResponseVOConverter jobBatchResponseVOConverter;

    public JobBatchService(JobTaskBatchDao jobTaskBatchDao,
                           WorkflowNodeDao workflowNodeDao, JobHandler jobHandler,
                           JobBatchResponseVOConverter jobBatchResponseVOConverter) {
        this.jobTaskBatchDao = jobTaskBatchDao;
        this.workflowNodeDao = workflowNodeDao;
        this.jobHandler = jobHandler;
        this.jobBatchResponseVOConverter = jobBatchResponseVOConverter;
    }


    public IPage<JobTaskBatchResponseVO> queryPage(Page<JobTaskBatch> page, QueryWrapper<JobTaskBatch> queryWrapper) {
        var resultPage = jobTaskBatchDao.findByQuery(queryWrapper, page, JobTaskBatchAndJobView.class);
        return resultPage.convert(jobBatchResponseVOConverter::convert);
    }


    public JobTaskBatchResponseVO findById(BigInteger id) {
        JobTaskBatchAndJobView jobTaskBatch = jobTaskBatchDao.findById(id, JobTaskBatchAndJobView.class).orElse(null);

        JobTaskBatchResponseVO jobTaskBatchResponseVO = jobBatchResponseVOConverter.convert(jobTaskBatch);

        if (SystemTaskType.WORKFLOW.equals(jobTaskBatch.getSystemTaskType())) {
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
