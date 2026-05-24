package com.old.silence.job.server.infrastructure.persistence.dao;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.old.silence.data.mybatis.projection.ProjectionMapperRepository;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.domain.model.WorkflowTaskBatch;
import com.old.silence.job.server.vo.JobBatchResponseDO;
import com.old.silence.job.server.vo.JobBatchSummaryResponseDO;

import java.math.BigInteger;
import java.util.List;

public interface JobTaskBatchDao extends ProjectionMapperRepository<JobTaskBatch, BigInteger> {

    List<JobBatchResponseDO> selectJobBatchListByIds(@Param("ew") Wrapper<JobTaskBatch> wrapper);

    List<JobBatchSummaryResponseDO> selectJobBatchSummaryList(@Param("ew") Wrapper<JobTaskBatch> wrapper);

    List<JobBatchSummaryResponseDO> selectWorkflowTaskBatchSummaryList(@Param("ew") Wrapper<WorkflowTaskBatch> wrapper);
}
