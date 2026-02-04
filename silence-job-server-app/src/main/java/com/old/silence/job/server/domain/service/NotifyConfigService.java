package com.old.silence.job.server.domain.service;

import cn.hutool.core.lang.Assert;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.server.api.assembler.NotifyConfigResponseVOMapper;
import com.old.silence.job.server.api.config.TenantContext;
import com.old.silence.job.server.domain.model.NotifyConfig;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.handler.SyncConfigHandler;
import com.old.silence.job.server.infrastructure.persistence.dao.NotifyConfigDao;
import com.old.silence.job.server.vo.NotifyConfigResponseVO;
import com.old.silence.core.util.CollectionUtils;


@Service
public class NotifyConfigService {

    private final NotifyConfigDao notifyConfigDao;
    private final NotifyConfigResponseVOMapper notifyConfigResponseVOMapper;

    public NotifyConfigService(NotifyConfigDao notifyConfigDao, NotifyConfigResponseVOMapper notifyConfigResponseVOMapper) {
        this.notifyConfigDao = notifyConfigDao;
        this.notifyConfigResponseVOMapper = notifyConfigResponseVOMapper;
    }

    public IPage<NotifyConfigResponseVO> getNotifyConfigList(Page<NotifyConfig> pageDTO, QueryWrapper<NotifyConfig> queryWrapper) {
        List<String> groupNames = List.of();
        queryWrapper.lambda().in(CollectionUtils.isNotEmpty(groupNames), NotifyConfig::getGroupName, groupNames);

        Page<NotifyConfig> notifyConfigPage = notifyConfigDao.selectPage(pageDTO, queryWrapper);

    return notifyConfigPage.convert(notifyConfigResponseVOMapper::convert);
    }

    public List<NotifyConfig> getNotifyConfigBySystemTaskTypeList(SystemTaskType systemTaskType) {
        return notifyConfigDao.selectList(new LambdaQueryWrapper<NotifyConfig>()
                .select(NotifyConfig::getId, NotifyConfig::getNotifyName)
                .eq(NotifyConfig::getSystemTaskType, systemTaskType)
                .orderByDesc(NotifyConfig::getId)
        );
    }

    public Boolean create(NotifyConfig notifyConfig) {

        Assert.isTrue(1 == notifyConfigDao.insert(notifyConfig),
                () -> new SilenceJobServerException("failed to insert notify. sceneConfig:[{}]",
                        JSON.toJSONString(notifyConfig)));
        return Boolean.TRUE;
    }

    public Boolean update(NotifyConfig notifyConfig) {
        Assert.notNull(notifyConfig.getId(), () -> new SilenceJobServerException("参数异常"));

        Assert.isTrue(1 == notifyConfigDao.updateById(notifyConfig),
                () -> new SilenceJobServerException("failed to update notify. sceneConfig:[{}]",
                        JSON.toJSONString(notifyConfig)));
        return Boolean.TRUE;
    }

    public NotifyConfigResponseVO getNotifyConfigDetail(BigInteger id) {
        NotifyConfig notifyConfig = notifyConfigDao.selectOne(new LambdaQueryWrapper<NotifyConfig>()
                .eq(NotifyConfig::getId, id));
        return notifyConfigResponseVOMapper.convert(notifyConfig);
    }

    public Boolean updateStatus(BigInteger id, Boolean status) {

        NotifyConfig notifyConfig = notifyConfigDao.selectOne(
                new LambdaQueryWrapper<NotifyConfig>()
                        .eq(NotifyConfig::getId, id)
        );
        Assert.notNull(notifyConfig, () -> new SilenceJobServerException("通知配置不存在"));

        var namespaceId = TenantContext.getTenantId();
        // 同步配置到客户端
        SyncConfigHandler.addSyncTask(notifyConfig.getGroupName(), namespaceId);

        NotifyConfig config = new NotifyConfig();
        config.setNotifyStatus(status);
        config.setUpdatedDate(Instant.now());
        int update = notifyConfigDao.update(config, new LambdaUpdateWrapper<NotifyConfig>()
                        .eq(NotifyConfig::getId, id)
                );

        return 1 == update;
    }

    public Boolean batchDeleteNotify(Set<BigInteger> ids) {
        return ids.size() == notifyConfigDao.deleteBatchIds(ids);
    }
}
