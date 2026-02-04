package com.old.silence.job.server.retry.task.support.schedule;

import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.alarm.Alarm;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 监控重试失败数据总量是否到达阈值
 *
 */
@Component
public class RetryErrorMoreThresholdAlarmSchedule extends AbstractRetryTaskAlarmSchedule implements Lifecycle {

    private static final String retryErrorMoreThresholdTextMessageFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 场景重试失败数量超过{}个</font>  \n" +
                    "> 空间ID:{}  \n" +
                    "> 组名称:{}  \n" +
                    "> 场景名称:{}  \n" +
                    "> 时间窗口:{} ~ {}  \n" +
                    "> **共计:{}**  \n";

    protected RetryErrorMoreThresholdAlarmSchedule(AccessTemplate accessTemplate,
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
        if (CollectionUtils.isEmpty(partitionTask.getNotifyIds())) {
            return;
        }

        Instant now = Instant.now();

        // x分钟内、x组、x场景进入任务到达最大重试次数的数据量
        long count = accessTemplate.getRetryAccess()
                .count(new LambdaQueryWrapper<Retry>()
                        .eq(Retry::getNamespaceId, partitionTask.getNamespaceId())
                        .between(Retry::getUpdatedDate, now.minus(30, ChronoUnit.MINUTES), now)
                        .eq(Retry::getGroupName, partitionTask.getGroupName())
                        .eq(Retry::getSceneName, partitionTask.getSceneName())
                        .eq(Retry::getRetryStatus, RetryStatus.MAX_COUNT.getValue())
                );

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
                            .text(retryErrorMoreThresholdTextMessageFormatter,
                                    EnvironmentUtils.getActiveProfile(),
                                    count,
                                    partitionTask.getNamespaceId(),
                                    partitionTask.getGroupName(),
                                    partitionTask.getSceneName(),
                                    DateUtils.format(now.minus(30, ChronoUnit.MINUTES),
                                            DateUtils.NORM_DATETIME_PATTERN),
                                    DateUtils.toNowFormat(DateUtils.NORM_DATETIME_PATTERN), count)
                            .title("{}环境 场景重试失败数量超过阈值", EnvironmentUtils.getActiveProfile())
                            .notifyAttribute(recipientInfo.getNotifyAttribute());
                    Alarm<AlarmContext> alarmType = SilenceJobAlarmFactory.getAlarmType(
                            recipientInfo.getNotifyType());
                    alarmType.asyncSendMessage(context);
                }

            }
        }
    }

    @Override
    protected RetryNotifyScene getNotifyScene() {
        return RetryNotifyScene.MAX_RETRY_ERROR;
    }


    @Override
    public String lockName() {
        return "retryErrorMoreThreshold";
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
