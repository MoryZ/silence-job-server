package com.old.silence.job.server.domain.service.config;

import java.util.List;

import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.job.common.enums.OperationTypeEnum;
import com.old.silence.job.server.domain.model.GroupConfig;


@Component
public class GroupConfigAccess extends AbstractConfigAccess<GroupConfig> {


    @Override
    public List<GroupConfig> list(LambdaQueryWrapper<GroupConfig> query) {
        return groupConfigDao.selectList(query);
    }

    @Override
    public int update(GroupConfig groupConfig, LambdaUpdateWrapper<GroupConfig> query) {
        return groupConfigDao.update(groupConfig, query);
    }

    @Override
    public int updateById(GroupConfig groupConfig) {
        return groupConfigDao.updateById(groupConfig);
    }

    @Override
    public int delete(LambdaQueryWrapper<GroupConfig> query) {
        return groupConfigDao.delete(query);
    }

    @Override
    public int insert(GroupConfig groupConfig) {
        return groupConfigDao.insert(groupConfig);
    }

    @Override
    public GroupConfig one(LambdaQueryWrapper<GroupConfig> query) {
        return groupConfigDao.selectOne(query);
    }

    @Override
    public Page<GroupConfig> listPage(Page<GroupConfig> iPage, LambdaQueryWrapper<GroupConfig> query) {
        return groupConfigDao.selectPage(iPage, query);
    }

    @Override
    public long count(LambdaQueryWrapper<GroupConfig> query) {
        return groupConfigDao.selectCount(query);
    }

    @Override
    public boolean supports(OperationTypeEnum operationType) {
        return OperationTypeEnum.GROUP.equals(operationType);
    }
}
