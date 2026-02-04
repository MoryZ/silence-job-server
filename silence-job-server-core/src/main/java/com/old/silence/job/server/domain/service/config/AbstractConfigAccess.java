package com.old.silence.job.server.domain.service.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.NodeType;
import com.old.silence.job.common.enums.RetryNotifyScene;
import com.old.silence.job.common.dto.ConfigDTO;
import com.old.silence.job.server.domain.model.GroupConfig;
import com.old.silence.job.server.domain.model.NotifyConfig;
import com.old.silence.job.server.domain.model.NotifyRecipient;
import com.old.silence.job.server.domain.model.RetrySceneConfig;
import com.old.silence.job.server.infrastructure.persistence.dao.GroupConfigDao;
import com.old.silence.job.server.infrastructure.persistence.dao.NotifyConfigDao;
import com.old.silence.job.server.infrastructure.persistence.dao.NotifyRecipientDao;
import com.old.silence.job.server.infrastructure.persistence.dao.SceneConfigDao;



public abstract class AbstractConfigAccess<T> implements ConfigAccess<T> {

    @Autowired
    protected NotifyConfigDao notifyConfigDao;
    @Autowired
    protected SceneConfigDao sceneConfigDao;
    @Autowired
    protected GroupConfigDao groupConfigDao;
    @Autowired
    protected NotifyRecipientDao notifyRecipientDao;

    protected RetrySceneConfig getByGroupNameAndSceneName(String groupName, String sceneName, String namespaceId) {
        return sceneConfigDao.selectOne(new LambdaQueryWrapper<RetrySceneConfig>()
                .eq(RetrySceneConfig::getNamespaceId, namespaceId)
                .eq(RetrySceneConfig::getGroupName, groupName)
                .eq(RetrySceneConfig::getSceneName, sceneName));
    }

    protected List<RetrySceneConfig> getByGroupNameAndSceneNameList(Set<String> groupNames, Set<String> sceneNames, Set<String> namespaceIds) {
        return sceneConfigDao.selectList(new LambdaQueryWrapper<RetrySceneConfig>()
                .in(RetrySceneConfig::getNamespaceId, namespaceIds)
                .in(RetrySceneConfig::getGroupName, groupNames)
                .in(RetrySceneConfig::getSceneName, sceneNames));
    }

    protected List<RetrySceneConfig> getSceneConfigs(String groupName) {
        return sceneConfigDao.selectList(new LambdaQueryWrapper<RetrySceneConfig>()
                .eq(RetrySceneConfig::getGroupName, groupName));
    }

    protected GroupConfig getByGroupName(String groupName, String namespaceId) {
        //TODO 不知道namespaceId 是否会自动填充
        return groupConfigDao.selectOne(new LambdaQueryWrapper<GroupConfig>()
                .eq(GroupConfig::getNamespaceId, namespaceId)
                .eq(GroupConfig::getGroupName, groupName));
    }

    protected List<NotifyConfig> getNotifyConfigs(String groupName, String namespaceId) {
        return notifyConfigDao.selectList(
                new LambdaQueryWrapper<NotifyConfig>()
                        .eq(NotifyConfig::getNamespaceId, namespaceId)
                        .eq(NotifyConfig::getGroupName, groupName)
                        .eq(NotifyConfig::getNotifyStatus, false)
        );
    }


    @Override
    public GroupConfig getGroupConfigByGroupName(String groupName, String namespaceId) {
        return getByGroupName(groupName, namespaceId);
    }

    @Override
    public RetrySceneConfig getSceneConfigByGroupNameAndSceneName(String groupName, String sceneName, String namespaceId) {
        return getByGroupNameAndSceneName(groupName, sceneName, namespaceId);
    }

    @Override
    public List<RetrySceneConfig> getSceneConfigByGroupNameAndSceneNameList(Set<String> groupNames, Set<String> sceneNames, Set<String> namespaceIds) {
        return getByGroupNameAndSceneNameList(groupNames, sceneNames, namespaceIds);
    }

    @Override
    public List<NotifyConfig> getNotifyListConfigByGroupName(String groupName, String namespaceId) {
        return getNotifyConfigs(groupName, namespaceId);
    }

    @Override
    public List<RetrySceneConfig> getSceneConfigByGroupName(String groupName) {
        return getSceneConfigs(groupName);
    }

    @Override
    public Set<String> getBlacklist(String groupName, String namespaceId) {

        GroupConfig groupConfig = getByGroupName(groupName, namespaceId);
        if (Objects.isNull(groupConfig)) {
            return new HashSet<>();
        }

        LambdaQueryWrapper<RetrySceneConfig> sceneConfigLambdaQueryWrapper = new LambdaQueryWrapper<RetrySceneConfig>()
                .select(RetrySceneConfig::getSceneName)
                .eq(RetrySceneConfig::getGroupName, groupName);

        if (groupConfig.getGroupStatus()) {
            sceneConfigLambdaQueryWrapper.eq(RetrySceneConfig::getSceneStatus, 500);
        }

        List<RetrySceneConfig> retrySceneConfigs = sceneConfigDao.selectList(sceneConfigLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(retrySceneConfigs)) {
            return new HashSet<>();
        }

        return retrySceneConfigs.stream().map(RetrySceneConfig::getSceneName).collect(Collectors.toSet());
    }

    @Override
    public List<GroupConfig> getAllConfigGroupList(String namespaceId) {
        List<GroupConfig> allSystemConfigGroupList = groupConfigDao.selectList(
                new LambdaQueryWrapper<GroupConfig>()
                        .eq(GroupConfig::getNamespaceId, namespaceId)
                        .orderByAsc(GroupConfig::getId));
        if (CollectionUtils.isEmpty(allSystemConfigGroupList)) {
            return new ArrayList<>();
        }

        return allSystemConfigGroupList;
    }

    @Override
    public List<RetrySceneConfig> getAllConfigSceneList() {
        List<RetrySceneConfig> allSystemConfigSceneList = sceneConfigDao.selectList(
                new LambdaQueryWrapper<RetrySceneConfig>().orderByAsc(RetrySceneConfig::getId));
        if (CollectionUtils.isEmpty(allSystemConfigSceneList)) {
            return new ArrayList<>();
        }
        return allSystemConfigSceneList;
    }

    @Override
    public Integer getConfigVersion(String groupName, String namespaceId) {
        GroupConfig groupConfig = getGroupConfigByGroupName(groupName, namespaceId);
        if (Objects.isNull(groupConfig)) {
            return 0;
        }

        return groupConfig.getVersion();
    }

    @Override
    public ConfigDTO getConfigInfo(String groupName, String namespaceId) {


        ConfigDTO configDTO = new ConfigDTO();
        configDTO.setVersion(getConfigVersion(groupName, namespaceId));

        List<NotifyConfig> notifyList = getNotifyListConfigByGroupName(groupName, namespaceId);

        List<ConfigDTO.Notify> notifies = new ArrayList<>();
        for (NotifyConfig notifyConfig : notifyList) {

            // 只选择客户端的通知配置即可
            RetryNotifyScene retryNotifyScene = RetryNotifyScene.getNotifyScene(notifyConfig.getNotifyScene().getValue(),
                    NodeType.CLIENT);
            JobNotifyScene jobNotifyScene = notifyConfig.getNotifyScene();
            if (Objects.isNull(retryNotifyScene) && Objects.isNull(jobNotifyScene)) {
                continue;
            }

            String recipientIds = notifyConfig.getRecipientIds();
            List<NotifyRecipient> notifyRecipients = notifyRecipientDao.selectBatchIds(
                    JSON.parseArray(recipientIds, Long.class));
            notifies.add(getNotify(notifyConfig, notifyRecipients, retryNotifyScene, jobNotifyScene));
        }

        configDTO.setNotifyList(notifies);

        List<RetrySceneConfig> retrySceneConfig = getSceneConfigByGroupName(groupName);

        List<ConfigDTO.Scene> sceneList = new ArrayList<>();
        for (RetrySceneConfig config : retrySceneConfig) {
            ConfigDTO.Scene scene = new ConfigDTO.Scene();
            scene.setSceneName(config.getSceneName());
            scene.setDdl(config.getDeadlineRequest());
            sceneList.add(scene);
        }

        configDTO.setSceneList(sceneList);
        return configDTO;
    }

    private static ConfigDTO.Notify getNotify(NotifyConfig notifyConfig, List<NotifyRecipient> notifyRecipients,
                                              RetryNotifyScene retryNotifyScene, JobNotifyScene jobNotifyScene) {
        List<ConfigDTO.Notify.Recipient> recipients = new ArrayList<>();
        for (NotifyRecipient notifyRecipient : notifyRecipients) {
            ConfigDTO.Notify.Recipient recipient = new ConfigDTO.Notify.Recipient();
            recipient.setNotifyAttribute(notifyRecipient.getNotifyAttribute());
            recipient.setNotifyType(notifyRecipient.getNotifyType());
            recipients.add(recipient);
        }

        ConfigDTO.Notify notify = new ConfigDTO.Notify();
        if (Objects.nonNull(retryNotifyScene)) {
            notify.setRetryNotifyScene(retryNotifyScene);
        }

        if (Objects.nonNull(jobNotifyScene)) {
            notify.setJobNotifyScene(jobNotifyScene);
        }

        notify.setNotifyThreshold(notifyConfig.getNotifyThreshold());
        notify.setRecipients(recipients);
        return notify;
    }
}
