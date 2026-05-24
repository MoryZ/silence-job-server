package com.old.silence.job.server.domain.service;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Sets;
import com.old.silence.core.exception.ResourceNotFoundException;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.server.api.assembler.RetrySceneConfigMapper;
import com.old.silence.job.server.api.assembler.RetrySceneConfigResponseVOMapper;
import com.old.silence.job.server.api.config.TenantContext;
import com.old.silence.job.server.common.dto.PartitionTask;
import com.old.silence.job.server.common.strategy.WaitStrategies;
import com.old.silence.job.server.common.util.CronUtils;
import com.old.silence.job.server.common.util.PartitionTaskUtils;
import com.old.silence.job.server.domain.model.Retry;
import com.old.silence.job.server.domain.model.RetryDeadLetter;
import com.old.silence.job.server.domain.model.RetrySceneConfig;
import com.old.silence.job.server.domain.model.RetrySummary;
import com.old.silence.job.server.dto.ExportSceneCommand;
import com.old.silence.job.server.dto.SceneConfigCommand;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.handler.GroupHandler;
import com.old.silence.job.server.handler.SyncConfigHandler;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryDeadLetterDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetrySceneConfigDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetrySceneConfigNotifyConfigRelationDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetrySummaryDao;
import com.old.silence.job.server.vo.RetrySceneConfigAndRetrySceneConfigNotifyConfigRelationView;
import com.old.silence.job.server.vo.RetrySceneConfigResponseVO;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;


@Service
@Validated
public class RetrySceneConfigService {

    private final RetrySceneConfigDao retrySceneConfigDao;
    private final RetrySceneConfigNotifyConfigRelationDao retrySceneConfigNotifyConfigRelationDao;
    private final RetryDao retryDao;
    private final RetryDeadLetterDao retryDeadLetterDao;
    private final GroupHandler groupHandler;
    private final RetrySummaryDao retrySummaryDao;
    private final RetrySceneConfigResponseVOMapper retrySceneConfigResponseVOMapper;
    private final RetrySceneConfigMapper retrySceneConfigMapper;

    public RetrySceneConfigService(RetrySceneConfigDao retrySceneConfigDao, RetrySceneConfigNotifyConfigRelationDao retrySceneConfigNotifyConfigRelationDao, RetryDao retryDao,
                                   RetryDeadLetterDao retryDeadLetterDao, GroupHandler groupHandler,
                                   RetrySummaryDao retrySummaryDao, RetrySceneConfigResponseVOMapper retrySceneConfigResponseVOMapper,
                                   RetrySceneConfigMapper retrySceneConfigMapper) {
        this.retrySceneConfigNotifyConfigRelationDao = retrySceneConfigNotifyConfigRelationDao;
        this.retryDeadLetterDao = retryDeadLetterDao;
        this.retrySceneConfigDao = retrySceneConfigDao;
        this.retryDao = retryDao;
        this.groupHandler = groupHandler;
        this.retrySummaryDao = retrySummaryDao;
        this.retrySceneConfigResponseVOMapper = retrySceneConfigResponseVOMapper;
        this.retrySceneConfigMapper = retrySceneConfigMapper;
    }

    private static void checkExecuteInterval(Byte backOff, String triggerInterval) {
        if (List.of(WaitStrategies.WaitStrategyEnum.FIXED.getValue(),
                WaitStrategies.WaitStrategyEnum.RANDOM.getValue()).contains(backOff.intValue())) {
            if (Integer.parseInt(triggerInterval) < 10) {
                throw new SilenceJobServerException("间隔时间不得小于10");
            }
        } else if (backOff.intValue() == WaitStrategies.WaitStrategyEnum.CRON.getValue()) {
            if (CronUtils.getExecuteInterval(triggerInterval) < 10 * 1000) {
                throw new SilenceJobServerException("间隔时间不得小于10");
            }
        }
    }

    public IPage<RetrySceneConfigResponseVO> queryPage(Page<RetrySceneConfig> pageDTO, QueryWrapper<RetrySceneConfig> queryWrapper) {
        var retrySceneConfigPage = retrySceneConfigDao.findByQuery(queryWrapper, pageDTO, RetrySceneConfigAndRetrySceneConfigNotifyConfigRelationView.class);
        return retrySceneConfigPage.convert(retrySceneConfigResponseVOMapper::convert);
    }

    public List<RetrySceneConfigResponseVO> getSceneConfigList(String groupName) {

        List<RetrySceneConfigAndRetrySceneConfigNotifyConfigRelationView> retrySceneConfigs = retrySceneConfigDao
                .findByQuery(new LambdaQueryWrapper<RetrySceneConfig>()
                        .eq(RetrySceneConfig::getGroupName, groupName)
                        .select(RetrySceneConfig::getSceneName,
                                RetrySceneConfig::getDescription, RetrySceneConfig::getMaxRetryCount)
                        .orderByDesc(RetrySceneConfig::getCreatedDate), RetrySceneConfigAndRetrySceneConfigNotifyConfigRelationView.class);

        return CollectionUtils.transformToList(retrySceneConfigs, retrySceneConfigResponseVOMapper::convert);
    }

    public Boolean create(RetrySceneConfig retrySceneConfig) {

        checkExecuteInterval(retrySceneConfig.getBackOff().getValue(), retrySceneConfig.getTriggerInterval());
        Assert.isTrue(0 == retrySceneConfigDao.selectCount(
                new LambdaQueryWrapper<RetrySceneConfig>()
                        .eq(RetrySceneConfig::getGroupName, retrySceneConfig.getGroupName())
                        .eq(RetrySceneConfig::getSceneName, retrySceneConfig.getSceneName())

        ), () -> new SilenceJobServerException("场景名称重复. {}", retrySceneConfig.getSceneName()));


        if (retrySceneConfig.getBackOff().getValue().intValue() == WaitStrategies.WaitStrategyEnum.DELAY_LEVEL.getValue()) {
            retrySceneConfig.setTriggerInterval(StrUtil.EMPTY);
        }

        if (retrySceneConfig.getCbStatus()) {
            checkExecuteInterval(retrySceneConfig.getCbTriggerType().getValue(), retrySceneConfig.getCbTriggerInterval());
            if (retrySceneConfig.getCbTriggerType().getValue().intValue() == WaitStrategies.WaitStrategyEnum.DELAY_LEVEL.getValue()) {
                retrySceneConfig.setCbTriggerInterval(StrUtil.EMPTY);
            }
        }

        Assert.isTrue(1 == retrySceneConfigDao.insert(retrySceneConfig),
                () -> new SilenceJobServerException("failed to insert scene. retrySceneConfig:[{}]",
                        JSON.toJSONString(retrySceneConfig)));

        var namespaceId = TenantContext.getTenantId();
        // 同步配置到客户端
        SyncConfigHandler.addSyncTask(retrySceneConfig.getGroupName(), namespaceId);

        if (CollectionUtils.isNotEmpty(retrySceneConfig.getNotifyRelations())) {
            retrySceneConfig.getNotifyRelations().forEach(notifyRelation -> {
                notifyRelation.setRetrySceneConfigId(retrySceneConfig.getId());
            });
            retrySceneConfigNotifyConfigRelationDao.insertBatch(retrySceneConfig.getNotifyRelations());
        }

        return Boolean.TRUE;
    }

    public Boolean update(RetrySceneConfig retrySceneConfig) {
        checkExecuteInterval(retrySceneConfig.getBackOff().getValue(), retrySceneConfig.getTriggerInterval());
        // 防止更新

        if (retrySceneConfig.getCbStatus()) {
            checkExecuteInterval(retrySceneConfig.getCbTriggerType().getValue(), retrySceneConfig.getCbTriggerInterval());
            if (retrySceneConfig.getCbTriggerType().getValue().intValue() == WaitStrategies.WaitStrategyEnum.DELAY_LEVEL.getValue()) {
                retrySceneConfig.setCbTriggerInterval(StrUtil.EMPTY);
            }
        }

        retrySceneConfig.setTriggerInterval(
                Optional.ofNullable(retrySceneConfig.getTriggerInterval()).orElse(StrUtil.EMPTY));
        try {
            retrySceneConfigDao.update(retrySceneConfig,
                    new LambdaUpdateWrapper<RetrySceneConfig>()
                            .eq(RetrySceneConfig::getGroupName, retrySceneConfig.getGroupName())
                            .eq(RetrySceneConfig::getSceneName, retrySceneConfig.getSceneName()));
        } catch (Exception e) {
            throw new SilenceJobServerException("failed to update scene. retrySceneConfig:[{}]",
                    JSON.toJSONString(retrySceneConfig));
        }

        var namespaceId = TenantContext.getTenantId();
        // 同步配置到客户端
        SyncConfigHandler.addSyncTask(retrySceneConfig.getGroupName(), namespaceId);

        if (CollectionUtils.isNotEmpty(retrySceneConfig.getNotifyRelations())) {
            retrySceneConfigNotifyConfigRelationDao.deleteByNotifyConfigId(retrySceneConfig.getId());
            retrySceneConfig.getNotifyRelations().forEach(notifyRelation -> {
                notifyRelation.setRetrySceneConfigId(retrySceneConfig.getId());
            });
            retrySceneConfigNotifyConfigRelationDao.insertBatch(retrySceneConfig.getNotifyRelations());
        } else {
            retrySceneConfigNotifyConfigRelationDao.deleteByNotifyConfigId(retrySceneConfig.getId());
        }
        return Boolean.TRUE;
    }

    public RetrySceneConfigResponseVO findById(BigInteger id) {
        RetrySceneConfigAndRetrySceneConfigNotifyConfigRelationView retrySceneConfig = retrySceneConfigDao.findById(id, RetrySceneConfigAndRetrySceneConfigNotifyConfigRelationView.class)
                .orElseThrow(ResourceNotFoundException::new);
        return retrySceneConfigResponseVOMapper.convert(retrySceneConfig);
    }

    public boolean updateStatus(BigInteger id, Boolean status) {

        RetrySceneConfig config = new RetrySceneConfig();
        config.setSceneStatus(status);

        return 1 == retrySceneConfigDao.update(config,
                new LambdaUpdateWrapper<RetrySceneConfig>()
                        .eq(RetrySceneConfig::getId, id)
        );
    }

    @Transactional
    public void importSceneConfig(List<SceneConfigCommand> requests) {
        batchSaveSceneConfig(requests);
    }

    public String exportSceneConfig(ExportSceneCommand exportSceneVO) {

        List<SceneConfigCommand> requestList = new ArrayList<>();

        PartitionTaskUtils.process(startId -> {
            List<RetrySceneConfig> sceneConfigs = retrySceneConfigDao
                    .selectPage(new PageDTO<>(0, 500), new LambdaQueryWrapper<RetrySceneConfig>()
                            .eq(Objects.nonNull(exportSceneVO.getSceneStatus()), RetrySceneConfig::getSceneStatus, exportSceneVO.getSceneStatus())
                            .eq(StrUtil.isNotBlank(exportSceneVO.getGroupName()),
                                    RetrySceneConfig::getGroupName, StrUtil.trim(exportSceneVO.getGroupName()))
                            .likeRight(StrUtil.isNotBlank(exportSceneVO.getSceneName()),
                                    RetrySceneConfig::getSceneName, StrUtil.trim(exportSceneVO.getSceneName()))
                            .in(CollectionUtils.isNotEmpty(exportSceneVO.getSceneIds()), RetrySceneConfig::getId, exportSceneVO.getSceneIds())
                            .ge(RetrySceneConfig::getId, startId)
                            .orderByAsc(RetrySceneConfig::getId)
                    ).getRecords();

            return CollectionUtils.transformToList(sceneConfigs, SceneConfigPartitionTask::new);
        }, partitionTasks -> {
            List<SceneConfigPartitionTask> partitionTaskList = (List<SceneConfigPartitionTask>) partitionTasks;
            var sceneConfigRequestVOS = CollectionUtils.transformToList(CollectionUtils.transformToList(partitionTaskList,
                    SceneConfigPartitionTask::getConfig), retrySceneConfigMapper::toSceneConfigRequestVO);
            requestList.addAll(sceneConfigRequestVOS);
        }, 0);

        return JSON.toJSONString(requestList);
    }

    @Transactional
    public boolean deleteByIds(Set<BigInteger> ids) {
        LambdaQueryWrapper<RetrySceneConfig> queryWrapper = new LambdaQueryWrapper<RetrySceneConfig>()
                .select(RetrySceneConfig::getSceneName, RetrySceneConfig::getGroupName)
                .eq(RetrySceneConfig::getSceneStatus, true)
                .in(RetrySceneConfig::getId, ids);

        List<RetrySceneConfig> sceneConfigs = retrySceneConfigDao.selectList(queryWrapper);
        Assert.notEmpty(sceneConfigs, () -> new SilenceJobServerException("删除重试场景失败, 请检查场景状态是否关闭状态"));

        Set<String> sceneNames = CollectionUtils.transformToSet(sceneConfigs, RetrySceneConfig::getSceneName);
        Set<String> groupNames = CollectionUtils.transformToSet(sceneConfigs, RetrySceneConfig::getGroupName);

        for (String groupName : groupNames) {
            List<Retry> retries = retryDao.selectPage(new PageDTO<>(1, 1),
                    new LambdaQueryWrapper<Retry>()
                            .eq(Retry::getGroupName, groupName)
                            .in(Retry::getSceneName, sceneNames)
                            .orderByAsc(Retry::getId)).getRecords();
            Assert.isTrue(CollectionUtils.isEmpty(retries),
                    () -> new SilenceJobServerException("删除重试场景失败, 存在【重试任务】请先删除【重试任务】在重试"));

            List<RetryDeadLetter> retryDeadLetters = retryDeadLetterDao.selectPage(new PageDTO<>(1, 1),
                    new LambdaQueryWrapper<RetryDeadLetter>()
                            .eq(RetryDeadLetter::getGroupName, groupName)
                            .in(RetryDeadLetter::getSceneName, sceneNames)
                            .orderByAsc(RetryDeadLetter::getId)).getRecords();
            Assert.isTrue(CollectionUtils.isEmpty(retryDeadLetters),
                    () -> new SilenceJobServerException("删除重试场景失败, 存在【死信任务】请先删除【死信任务】在重试"));
        }

        Assert.isTrue(ids.size() == retrySceneConfigDao.delete(queryWrapper),
                () -> new SilenceJobServerException("删除重试场景失败, 请检查场景状态是否关闭状态"));

        List<RetrySummary> retrySummaries = retrySummaryDao.selectList(
                new LambdaQueryWrapper<RetrySummary>()
                        .select(RetrySummary::getId)
                        .in(RetrySummary::getGroupName, groupNames)
                        .in(RetrySummary::getSceneName, sceneNames)
        );

        if (CollectionUtils.isNotEmpty(retrySummaries)) {
            Assert.isTrue(retrySummaries.size() == retrySummaryDao.deleteBatchIds(CollectionUtils.transformToSet(retrySummaries, RetrySummary::getId))
                    , () -> new SilenceJobServerException("删除汇总表数据失败"));
        }

        return Boolean.TRUE;
    }

    private void batchSaveSceneConfig(List<SceneConfigCommand> requests) {

        Set<String> groupNameSet = Sets.newHashSet();
        Set<String> sceneNameSet = Sets.newHashSet();
        for (SceneConfigCommand requestVO : requests) {
            checkExecuteInterval(requestVO.getBackOff().getValue(), requestVO.getTriggerInterval());
            if (requestVO.getCbStatus()) {
                checkExecuteInterval(requestVO.getCbTriggerType().getValue(), requestVO.getCbTriggerInterval());
            }
            groupNameSet.add(requestVO.getGroupName());
            sceneNameSet.add(requestVO.getSceneName());
        }

        groupHandler.validateGroupExistence(groupNameSet);

        List<RetrySceneConfig> sceneConfigs = retrySceneConfigDao.selectList(
                new LambdaQueryWrapper<RetrySceneConfig>()
                        .select(RetrySceneConfig::getSceneName)
                        .in(RetrySceneConfig::getGroupName, groupNameSet)
                        .in(RetrySceneConfig::getSceneName, sceneNameSet));

        Assert.isTrue(CollectionUtils.isEmpty(sceneConfigs), () -> new SilenceJobServerException("导入失败. 原因:场景{}已存在",
                CollectionUtils.transformToSet(sceneConfigs, RetrySceneConfig::getSceneName)));

        List<RetrySceneConfig> retrySceneConfigs = CollectionUtils.transformToList(requests, retrySceneConfigMapper::convert);
        for (RetrySceneConfig retrySceneConfig : retrySceneConfigs) {
            if (retrySceneConfig.getBackOff().getValue().intValue() == WaitStrategies.WaitStrategyEnum.DELAY_LEVEL.getValue()) {
                retrySceneConfig.setTriggerInterval(StrUtil.EMPTY);
            }

            if (retrySceneConfig.getCbStatus()) {
                if (retrySceneConfig.getCbTriggerType().getValue().intValue() == WaitStrategies.WaitStrategyEnum.DELAY_LEVEL.getValue()) {
                    retrySceneConfig.setCbTriggerInterval(StrUtil.EMPTY);
                }
            }

            Assert.isTrue(1 == retrySceneConfigDao.insert(retrySceneConfig),
                    () -> new SilenceJobServerException("failed to insert scene. retrySceneConfig:[{}]",
                            JSON.toJSONString(retrySceneConfig)));
        }

    }

    private static class SceneConfigPartitionTask extends PartitionTask {
        // 这里就直接放RetrySceneConfig为了后面若加字段不需要再这里在调整了
        private final RetrySceneConfig config;

        public SceneConfigPartitionTask(RetrySceneConfig config) {
            this.config = config;
            setId(config.getId());
        }

        public RetrySceneConfig getConfig() {
            return config;
        }
    }

}
