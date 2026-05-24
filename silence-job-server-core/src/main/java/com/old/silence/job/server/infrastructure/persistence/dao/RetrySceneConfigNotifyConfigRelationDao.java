package com.old.silence.job.server.infrastructure.persistence.dao;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.old.silence.job.server.domain.model.NotifyConfigRecipientRelation;
import com.old.silence.job.server.domain.model.RetrySceneConfigNotifyConfigRelation;

import java.math.BigInteger;
import java.util.List;

/**
 * <p>
 * 重试场景 告警配置 关联关系 Mapper 接口
 * </p>
 *
 */
@Mapper
public interface RetrySceneConfigNotifyConfigRelationDao extends BaseMapper<RetrySceneConfigNotifyConfigRelation> {

    int insertBatch(@Param("list") List<RetrySceneConfigNotifyConfigRelation> list);

    @Delete("DELETE FROM sj_retry_scene_config_notify_config_relation WHERE notify_config_id = #{notifyConfigId}")
    int deleteByNotifyConfigId(BigInteger notifyConfigId);
}
