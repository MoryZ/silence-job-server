package com.old.silence.job.server.retry.task.support.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import com.google.common.collect.Lists;
import com.old.silence.core.enums.EnumValueFactory;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.alarm.AlarmContext;
import com.old.silence.job.common.enums.RetryNotifyScene;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.common.util.EnvironmentUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;
import com.old.silence.job.server.common.alarm.AbstractRetryAlarm;
import com.old.silence.job.server.common.dto.NotifyConfigInfo;
import com.old.silence.job.server.common.dto.RetryAlarmInfo;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.retry.task.dto.RetryTaskFailAlarmEventDTO;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;
import com.old.silence.job.server.retry.task.support.event.RetryTaskFailAlarmEvent;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 重试任务失败监听器
 *
 */
@Component

public class RetryTaskFailAlarmListener extends
        AbstractRetryAlarm<RetryTaskFailAlarmEvent> implements Runnable, Lifecycle {

    /**
     * 死信告警数据
     */
    private final LinkedBlockingQueue<RetryTaskFailAlarmEventDTO> queue = new LinkedBlockingQueue<>(1000);

    private static final String retryTaskDeadTextMessagesFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 重试任务执行失败</font>  \n" +
                    "> 任务重试次数:{}  \n" +
                    "> 通知场景:{}  \n" +
                    "> 空间ID:{}  \n" +
                    "> 组名称:{}  \n" +
                    "> 执行器名称:{}  \n" +
                    "> 场景名称:{}  \n" +
                    "> 业务数据:{}  \n" +
                    "> 时间:{}  \n" +
                    "> 失败原因:{}  \n";

    @Override
    protected List<SystemTaskType> getSystemTaskType() {
        return List.of(SystemTaskType.RETRY);
    }

    @Override
    protected List<RetryAlarmInfo> poll() throws InterruptedException {
        // 无数据时阻塞线程
        RetryTaskFailAlarmEventDTO retryTaskFailAlarmEventDO = queue.poll(100, TimeUnit.MILLISECONDS);
        if (Objects.isNull(retryTaskFailAlarmEventDO)) {
            return Lists.newArrayList();
        }

        // 拉取200条
        List<RetryTaskFailAlarmEventDTO> lists = Lists.newArrayList(retryTaskFailAlarmEventDO);
        queue.drainTo(lists, 200);

        // 数据类型转换
        return CollectionUtils.transformToList(lists, RetryTaskConverter.INSTANCE::toRetryTaskFailAlarmEventDTO);
    }

    @Override
    @TransactionalEventListener(fallbackExecution = true, phase = TransactionPhase.AFTER_COMPLETION)
    public void doOnApplicationEvent(RetryTaskFailAlarmEvent retryTaskFailAlarmEvent) {
        if (!queue.offer(retryTaskFailAlarmEvent.getRetryTaskFailAlarmEventDTO())) {
            SilenceJobLog.LOCAL.warn("任务重试失败告警队列已满");
        }
    }

    @Override
    protected AlarmContext buildAlarmContext(final RetryAlarmInfo retryAlarmInfo, final NotifyConfigInfo notifyConfig) {

        // 预警
        return AlarmContext.build().text(retryTaskDeadTextMessagesFormatter,
                        EnvironmentUtils.getActiveProfile(),
                        notifyConfig.getNotifyThreshold(),
                        EnumValueFactory.getRequired(RetryNotifyScene.class,retryAlarmInfo.getNotifyScene()).getDescription(),
                        retryAlarmInfo.getNamespaceId(),
                        retryAlarmInfo.getGroupName(),
                        retryAlarmInfo.getExecutorName(),
                        retryAlarmInfo.getSceneName(),
                        retryAlarmInfo.getArgsStr(),
                        DateUtils.toNowFormat(DateUtils.NORM_DATETIME_PATTERN),
                        retryAlarmInfo.getReason())
                .title("组:[{}] 场景:[{}] 环境重试任务失败",
                        retryAlarmInfo.getGroupName(), retryAlarmInfo.getSceneName());
    }

    @Override
    protected void startLog() {
        SilenceJobLog.LOCAL.info("RetryTaskFailAlarmListener started");
    }

    @Override
    protected int getNotifyScene() {
        return RetryNotifyScene.RETRY_TASK_FAIL_ERROR.getValue().intValue();
    }
}
