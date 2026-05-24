package com.old.silence.job.server.infrastructure.persistence.dao;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.old.silence.job.server.domain.model.JobNotifyConfigRelation;
import com.old.silence.job.server.domain.model.NotifyConfigRecipientRelation;

import java.math.BigInteger;
import java.util.List;

/**
 * <p>
 * 告警配置 通知人 关联关系 Mapper 接口
 * </p>
 *
 */
@Mapper
public interface NotifyConfigRecipientRelationDao extends BaseMapper<NotifyConfigRecipientRelation> {

    int insertBatch(@Param("list") List<NotifyConfigRecipientRelation> list);

    @Delete("DELETE FROM sj_notify_config_recipient_relation WHERE notify_config_id = #{notifyConfigId}")
    int deleteByNotifyConfigId(BigInteger notifyConfigId);
}
