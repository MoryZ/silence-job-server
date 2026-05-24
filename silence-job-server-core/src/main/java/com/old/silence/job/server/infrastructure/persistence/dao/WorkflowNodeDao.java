package com.old.silence.job.server.infrastructure.persistence.dao;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.old.silence.job.server.domain.model.WorkflowNode;

/**
 * <p>
 * 工作流节点 Mapper 接口
 * </p>
 *
 */
@Mapper
public interface WorkflowNodeDao extends BaseMapper<WorkflowNode> {

}
