package com.old.silence.job.server.domain.service.config;

import java.util.List;

import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.job.common.enums.OperationTypeEnum;
import com.old.silence.job.server.domain.model.RetrySceneConfig;


@Component
public class SceneConfigAccess extends AbstractConfigAccess<RetrySceneConfig> {


    @Override
    public List<RetrySceneConfig> list(LambdaQueryWrapper<RetrySceneConfig> query) {
        return sceneConfigDao.selectList(query);
    }

    @Override
    public int update(RetrySceneConfig retrySceneConfig, LambdaUpdateWrapper<RetrySceneConfig> query) {
        return sceneConfigDao.update(retrySceneConfig, query);
    }

    @Override
    public int updateById(RetrySceneConfig retrySceneConfig) {
        return sceneConfigDao.updateById(retrySceneConfig);
    }

    @Override
    public int delete(LambdaQueryWrapper<RetrySceneConfig> query) {
        return sceneConfigDao.delete(query);
    }

    @Override
    public int insert(RetrySceneConfig retrySceneConfig) {
        return sceneConfigDao.insert(retrySceneConfig);
    }

    @Override
    public RetrySceneConfig one(LambdaQueryWrapper<RetrySceneConfig> query) {
        return sceneConfigDao.selectOne(query);
    }

    @Override
    public Page<RetrySceneConfig> listPage(Page<RetrySceneConfig> iPage, LambdaQueryWrapper<RetrySceneConfig> query) {
        return sceneConfigDao.selectPage(iPage, query);
    }

    @Override
    public long count(LambdaQueryWrapper<RetrySceneConfig> query) {
        return sceneConfigDao.selectCount(query);
    }


    @Override
    public boolean supports(OperationTypeEnum operationType) {
        return OperationTypeEnum.SCENE.equals(operationType);
    }
}
