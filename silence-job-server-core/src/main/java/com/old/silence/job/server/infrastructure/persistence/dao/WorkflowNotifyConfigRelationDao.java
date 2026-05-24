package com.old.silence.job.server.infrastructure.persistence.dao;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.old.silence.job.server.domain.model.WorkflowNotifyConfigRelation;

import java.math.BigInteger;
import java.util.List;

/**
 * <p>
 * 工作流 配置 关联关系 Mapper 接口
 * </p>
 *
 */
@Mapper
public interface WorkflowNotifyConfigRelationDao extends BaseMapper<WorkflowNotifyConfigRelation> {

    int insertBatch(@Param("list") List<WorkflowNotifyConfigRelation> list);

    @Delete("DELETE FROM sj_workflow_notify_config_relation WHERE workflow_id = #{workflowId}")
    int deleteByWorkflowId(BigInteger workflowId);
}
