package com.old.silence.job.server.infrastructure.persistence.dao;

import org.apache.ibatis.annotations.Param;
import com.old.silence.data.mybatis.projection.ProjectionMapperRepository;
import com.old.silence.job.server.domain.model.Workflow;

import java.math.BigInteger;
import java.util.List;

/**
 * <p>
 * 工作流 Mapper 接口
 * </p>
 *
 */
public interface WorkflowDao extends ProjectionMapperRepository<Workflow, BigInteger> {

    int updateBatchNextTriggerAtById(@Param("list") List<Workflow> list);
}
