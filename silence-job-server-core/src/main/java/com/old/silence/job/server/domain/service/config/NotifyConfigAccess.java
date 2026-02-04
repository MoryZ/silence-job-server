package com.old.silence.job.server.domain.service.config;

import java.util.List;

import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.job.common.enums.OperationTypeEnum;
import com.old.silence.job.server.domain.model.NotifyConfig;


@Component
public class NotifyConfigAccess extends AbstractConfigAccess<NotifyConfig> {


    @Override
    public List<NotifyConfig> list(LambdaQueryWrapper<NotifyConfig> query) {
        return notifyConfigDao.selectList(query);
    }

    @Override
    public int update(NotifyConfig notifyConfig, LambdaUpdateWrapper<NotifyConfig> query) {
        return notifyConfigDao.update(notifyConfig, query);
    }

    @Override
    public int updateById(NotifyConfig notifyConfig) {
        return notifyConfigDao.updateById(notifyConfig);
    }

    @Override
    public int delete(LambdaQueryWrapper<NotifyConfig> query) {
        return notifyConfigDao.delete(query);
    }

    @Override
    public int insert(NotifyConfig notifyConfig) {
        return notifyConfigDao.insert(notifyConfig);
    }

    @Override
    public NotifyConfig one(LambdaQueryWrapper<NotifyConfig> query) {
        return notifyConfigDao.selectOne(query);
    }

    @Override
    public Page<NotifyConfig> listPage(Page<NotifyConfig> iPage, LambdaQueryWrapper<NotifyConfig> query) {
        return notifyConfigDao.selectPage(iPage, query);
    }

    @Override
    public long count(LambdaQueryWrapper<NotifyConfig> query) {
        return notifyConfigDao.selectCount(query);
    }

    @Override
    public boolean supports(OperationTypeEnum operationType) {
        return OperationTypeEnum.NOTIFY.equals(operationType);
    }
}
