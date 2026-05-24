package com.old.silence.job.server.infrastructure.persistence.dao;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.old.silence.job.server.domain.model.JobNotifyConfigRelation;

import java.math.BigInteger;
import java.util.List;

/**
 * <p>
 * job 告警配置 关联关系  Mapper 接口
 * </p>
 *
 */
@Mapper
public interface JobNotifyConfigRelationDao extends BaseMapper<JobNotifyConfigRelation> {

    int insertBatch(@Param("list") List<JobNotifyConfigRelation> list);

    @Delete("DELETE FROM sj_job_notify_config_relation WHERE job_id = #{jobId}")
    int deleteByJobId(BigInteger jobId);
}
