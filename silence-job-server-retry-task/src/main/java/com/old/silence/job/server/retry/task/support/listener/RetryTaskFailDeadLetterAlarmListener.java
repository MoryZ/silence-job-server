package com.old.silence.job.server.retry.task.support.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
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
import com.old.silence.job.server.retry.task.dto.RetryTaskFailDeadLetterAlarmEventDTO;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;
import com.old.silence.job.server.retry.task.support.event.RetryTaskFailDeadLetterAlarmEvent;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 重试任务失败进入死信队列监听器
 *
 */
@Component

public class RetryTaskFailDeadLetterAlarmListener extends
        AbstractRetryAlarm<RetryTaskFailDeadLetterAlarmEvent> implements Runnable, Lifecycle {

    /**
     * 死信告警数据
     */
    private final LinkedBlockingQueue<List<RetryTaskFailDeadLetterAlarmEventDTO>> queue = new LinkedBlockingQueue<>(1000);

    private static final String retryTaskDeadTextMessagesFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 重试任务失败进入死信队列</font>  \n" +
                    "> 空间ID:{}  \n" +
                    "> 组名称:{}  \n" +
                    "> 执行器名称:{}  \n" +
                    "> 场景名称:{}  \n" +
                    "> 业务数据:{}  \n" +
                    "> 时间:{}  \n";

    @Override
    protected List<SystemTaskType> getSystemTaskType() {
        return List.of(SystemTaskType.RETRY);
    }

    @Override
    protected List<RetryAlarmInfo> poll() throws InterruptedException {
        // 无数据时阻塞线程
        List<RetryTaskFailDeadLetterAlarmEventDTO> allRetryDeadLetterList = queue.poll(100, TimeUnit.MILLISECONDS);
        if (CollectionUtils.isEmpty(allRetryDeadLetterList)) {
            return List.of();
        }

        return RetryTaskConverter.INSTANCE.toRetryAlarmInfos(allRetryDeadLetterList);
    }

    @Override
    @TransactionalEventListener(fallbackExecution = true, phase = TransactionPhase.AFTER_COMPLETION)
    public void doOnApplicationEvent(RetryTaskFailDeadLetterAlarmEvent event) {
        if (!queue.offer(event.getRetryDeadLetters())) {
            SilenceJobLog.LOCAL.warn("任务重试失败进入死信队列告警队列已满");
        }
    }

    @Override
    protected AlarmContext buildAlarmContext(final RetryAlarmInfo retryAlarmInfo, final NotifyConfigInfo notifyConfig) {

        // 预警
        return AlarmContext.build().text(retryTaskDeadTextMessagesFormatter,
                        EnvironmentUtils.getActiveProfile(),
                        retryAlarmInfo.getNamespaceId(),
                        retryAlarmInfo.getGroupName(),
                        retryAlarmInfo.getExecutorName(),
                        retryAlarmInfo.getSceneName(),
                        retryAlarmInfo.getArgsStr(),
                        DateUtils.toNowFormat(DateUtils.NORM_DATETIME_PATTERN))
                .title("组:[{}] 场景:[{}] 环境重试任务失败进入死信队列",
                        retryAlarmInfo.getGroupName(), retryAlarmInfo.getSceneName());
    }

    @Override
    protected void startLog() {
        SilenceJobLog.LOCAL.info("RetryTaskFailDeadLetterAlarmListener started");
    }

    @Override
    protected int getNotifyScene() {
        return RetryNotifyScene.RETRY_TASK_ENTER_DEAD_LETTER.getValue().intValue();
    }
}
