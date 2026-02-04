package com.old.silence.job.server.infrastructure.persistence.dao;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.old.silence.job.server.domain.model.SystemUserPermission;

/**
 * <p>
 * 系统用户权限表 Mapper 接口
 * </p>
 *
 */
@Mapper
public interface SystemUserPermissionDao extends BaseMapper<SystemUserPermission> {

}
