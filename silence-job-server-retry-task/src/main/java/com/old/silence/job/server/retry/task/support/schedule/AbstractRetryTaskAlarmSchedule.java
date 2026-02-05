package com.old.silence.job.server.retry.task.support.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.RetryNotifyScene;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.server.common.Lifecycle;
import com.old.silence.job.server.common.dto.PartitionTask;
import com.old.silence.job.server.common.schedule.AbstractSchedule;
import com.old.silence.job.server.common.util.PartitionTaskUtils;
import com.old.silence.job.server.domain.model.NotifyConfig;
import com.old.silence.job.server.domain.model.NotifyRecipient;
import com.old.silence.job.server.domain.model.RetrySceneConfig;
import com.old.silence.job.server.infrastructure.persistence.dao.NotifyConfigDao;
import com.old.silence.job.server.infrastructure.persistence.dao.NotifyRecipientDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetrySceneConfigDao;
import com.old.silence.job.server.retry.task.dto.NotifyConfigDTO;
import com.old.silence.job.server.retry.task.dto.RetrySceneConfigPartitionTask;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public abstract class AbstractRetryTaskAlarmSchedule extends AbstractSchedule implements Lifecycle {
    private final RetrySceneConfigDao retrySceneConfigDao;
    private final NotifyConfigDao notifyConfigDao;
    private final NotifyRecipientDao notifyRecipientDao;

    protected AbstractRetryTaskAlarmSchedule(RetrySceneConfigDao retrySceneConfigDao, NotifyConfigDao notifyConfigDao, NotifyRecipientDao notifyRecipientDao) {
        this.retrySceneConfigDao = retrySceneConfigDao;
        this.notifyConfigDao = notifyConfigDao;
        this.notifyRecipientDao = notifyRecipientDao;
    }

    @Override
    protected void doExecute() {
        PartitionTaskUtils.process(this::queryPartitionList, this::doHandler, 0);
    }

    /**
     * 循环场景信息
     *
     * @param partitionTasks 需要告警的场景信息
     */
    private void doHandler(List<? extends PartitionTask> partitionTasks) {

        // 处理通知信息
        Map<BigInteger, NotifyConfigDTO> notifyConfigInfo = getNotifyConfigInfo((List<RetrySceneConfigPartitionTask>) partitionTasks);
        if (notifyConfigInfo.isEmpty()) {
            return;
        }

        for (PartitionTask partitionTask : partitionTasks) {
            doSendAlarm((RetrySceneConfigPartitionTask) partitionTask, notifyConfigInfo);
        }
    }

    protected abstract void doSendAlarm(RetrySceneConfigPartitionTask partitionTask, Map<BigInteger, NotifyConfigDTO> notifyConfigInfo);

    protected abstract RetryNotifyScene getNotifyScene();

    /**
     * 循环场景信息
     *
     * @param startId 偏移id
     * @return 需要处理的场景列表
     */
    protected List<RetrySceneConfigPartitionTask> queryPartitionList(Long startId) {
        List<RetrySceneConfig> retrySceneConfigList = retrySceneConfigDao
                .selectPage(new PageDTO<>(0, 500),
                        new LambdaQueryWrapper<RetrySceneConfig>()
                                .gt(RetrySceneConfig::getId, startId)
                                .eq(RetrySceneConfig::getSceneStatus, true)
                                .orderByAsc(RetrySceneConfig::getId)
                ).getRecords();
        return RetryTaskConverter.INSTANCE.toRetrySceneConfigPartitionTask(retrySceneConfigList);
    }


    /**
     * 获取通知信息
     * @param partitionTasks 本次需要处理的场景列表
     * @return Map<Long(通知配置id), NotifyConfigDTO(配置信息)>
     */
    protected Map<BigInteger, NotifyConfigDTO> getNotifyConfigInfo(List<RetrySceneConfigPartitionTask> partitionTasks) {

        // 提前通知配置id
        Set<BigInteger> retryNotifyIds = partitionTasks.stream()
                .map(RetrySceneConfigPartitionTask::getNotifyIds)
                .filter(CollectionUtils::isNotEmpty)
                .reduce((a, b) -> {
                    HashSet<BigInteger> set = Sets.newHashSet();
                    set.addAll(a);
                    set.addAll(b);
                    return set;
                }).orElse(new HashSet<>());

        if (CollectionUtils.isEmpty(retryNotifyIds)) {
            return Maps.newHashMap();
        }

        // 从DB中获取通知配置信息
        List<NotifyConfigDTO> notifyConfigs = RetryTaskConverter.INSTANCE.toNotifyConfigDTO(notifyConfigDao
                .selectList(new LambdaQueryWrapper<NotifyConfig>()
                        .in(NotifyConfig::getId, retryNotifyIds)
                        .eq(NotifyConfig::getNotifyStatus, true)
                        .eq(NotifyConfig::getNotifyScene, getNotifyScene())
                        .orderByAsc(NotifyConfig::getId)));
        if (CollectionUtils.isEmpty(notifyConfigs)) {
            return Maps.newHashMap();
        }

        // 提前通知人信息
        Set<BigInteger> recipientIds = notifyConfigs.stream()
                .map(NotifyConfigDTO::getRecipientIds)
                .filter(CollectionUtils::isNotEmpty)
                .reduce((a, b) -> {
                    HashSet<BigInteger> set = Sets.newHashSet();
                    set.addAll(a);
                    set.addAll(b);
                    return set;
                }).orElse(new HashSet<>());

        if (CollectionUtils.isEmpty(recipientIds)) {
            return Maps.newHashMap();
        }

        // 从DB中获取通知人信息
        List<NotifyRecipient> notifyRecipients = notifyRecipientDao.selectBatchIds(recipientIds);
        Map<BigInteger, NotifyRecipient> recipientMap = StreamUtils.toIdentityMap(notifyRecipients, NotifyRecipient::getId);

        Map<BigInteger, NotifyConfigDTO> notifyConfigMap = Maps.newHashMap();
        for (final NotifyConfigDTO notifyConfigDTO : notifyConfigs) {

            List<NotifyConfigDTO.RecipientInfo> recipientList = StreamUtils.toList(notifyConfigDTO.getRecipientIds(),
                    recipientId -> {
                        NotifyRecipient notifyRecipient = recipientMap.get(recipientId);
                        if (Objects.isNull(notifyRecipient)) {
                            return null;
                        }

                        NotifyConfigDTO.RecipientInfo recipientInfo = new NotifyConfigDTO.RecipientInfo();
                        recipientInfo.setNotifyType(notifyRecipient.getNotifyType());
                        recipientInfo.setNotifyAttribute(notifyRecipient.getNotifyAttribute());

                        return recipientInfo;
                    });

            notifyConfigDTO.setRecipientInfos(recipientList);
            notifyConfigMap.put(notifyConfigDTO.getId(), notifyConfigDTO);
        }

        return notifyConfigMap;
    }

}
