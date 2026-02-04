package com.old.silence.job.server.retry.task.support.schedule;

import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.job.common.alarm.AlarmContext;
import com.old.silence.job.common.alarm.SilenceJobAlarmFactory;
import com.old.silence.job.common.enums.RetryNotifyScene;
import com.old.silence.job.common.enums.RetryStatus;
import com.old.silence.job.common.util.EnvironmentUtils;
import com.old.silence.job.server.common.Lifecycle;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.domain.model.Retry;
import com.old.silence.job.server.domain.service.AccessTemplate;
import com.old.silence.job.server.infrastructure.persistence.dao.NotifyRecipientDao;
import com.old.silence.job.server.retry.task.dto.NotifyConfigDTO;
import com.old.silence.job.server.retry.task.dto.RetrySceneConfigPartitionTask;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


/**
 * 监控重试表中数据总量是否到达阈值
 *
 */
@Component

public class RetryTaskMoreThresholdAlarmSchedule extends AbstractRetryTaskAlarmSchedule implements Lifecycle {

    private static final String retryTaskMoreThresholdTextMessageFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 场景重试数量超过{}个</font>  \n" +
                    "> 空间ID:{}  \n" +
                    "> 组名称:{}  \n" +
                    "> 场景名称:{}  \n" +
                    "> 告警时间:{}  \n" +
                    "> **共计:{}**  \n";

    protected RetryTaskMoreThresholdAlarmSchedule(AccessTemplate accessTemplate,
                                                  NotifyRecipientDao notifyRecipientDao) {
        super(accessTemplate, notifyRecipientDao);
    }

    @Override
    public void start() {
        taskScheduler.scheduleWithFixedDelay(this::execute, Instant.now(), Duration.parse("PT10M"));
    }

    @Override
    public void close() {
    }

    @Override
    protected void doSendAlarm(RetrySceneConfigPartitionTask partitionTask, Map<BigInteger, NotifyConfigDTO> notifyConfigInfo) {

        // x分钟内、x组、x场景进入重试任务的数据量
        long count = accessTemplate.getRetryAccess()
                .count(new LambdaQueryWrapper<Retry>()
                                .eq(Retry::getNamespaceId, partitionTask.getNamespaceId())
                                .eq(Retry::getGroupName, partitionTask.getGroupName())
                                .eq(Retry::getSceneName, partitionTask.getSceneName())
                                .eq(Retry::getRetryStatus, RetryStatus.RUNNING.getValue()));
        for (BigInteger notifyId : partitionTask.getNotifyIds()) {
            NotifyConfigDTO notifyConfigDTO = notifyConfigInfo.get(notifyId);
            if (notifyConfigDTO == null) {
                continue;
            }

            if (notifyConfigDTO.getNotifyThreshold() > 0 && count >= notifyConfigDTO.getNotifyThreshold()) {
                List<NotifyConfigDTO.RecipientInfo> recipientInfos = notifyConfigDTO.getRecipientInfos();
                for (final NotifyConfigDTO.RecipientInfo recipientInfo : recipientInfos) {
                    if (Objects.isNull(recipientInfo)) {
                        continue;
                    }
                    // 预警
                    AlarmContext context = AlarmContext.build()
                            .text(retryTaskMoreThresholdTextMessageFormatter,
                                    EnvironmentUtils.getActiveProfile(),
                                    count,
                                    partitionTask.getNamespaceId(),
                                    partitionTask.getGroupName(),
                                    partitionTask.getSceneName(),
                                    DateUtils.toNowFormat(DateUtils.NORM_DATETIME_PATTERN),
                                    count)
                            .title("{}环境 场景重试数量超过阈值", EnvironmentUtils.getActiveProfile())
                            .notifyAttribute(recipientInfo.getNotifyAttribute());
                    Optional.ofNullable(SilenceJobAlarmFactory.getAlarmType(recipientInfo.getNotifyType()))
                            .ifPresent(alarmType -> alarmType.asyncSendMessage(context));

                }
            }
        }

    }

    @Override
    protected RetryNotifyScene getNotifyScene() {
        return RetryNotifyScene.MAX_RETRY;
    }

    @Override
    public String lockName() {
        return "retryTaskMoreThreshold";
    }

    @Override
    public String lockAtMost() {
        return "PT10M";
    }

    @Override
    public String lockAtLeast() {
        return "PT1M";
    }
}
