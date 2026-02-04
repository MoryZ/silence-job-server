package com.old.silence.job.server.job.task.support.alarm.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import com.google.common.collect.Lists;
import com.old.silence.core.enums.EnumValueFactory;
import com.old.silence.job.common.alarm.AlarmContext;
import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.common.util.EnvironmentUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.alarm.AbstractJobAlarm;
import com.old.silence.job.server.common.dto.JobAlarmInfo;
import com.old.silence.job.server.common.dto.NotifyConfigInfo;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.job.task.dto.JobTaskFailAlarmEventDTO;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.job.task.support.alarm.event.JobTaskFailAlarmEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


@Component
public class JobTaskFailAlarmListener extends AbstractJobAlarm<JobTaskFailAlarmEvent> {

    private static final String MESSAGES_FORMATTER = "<font face=微软雅黑 color=#ff0000 size=4>{}环境 Job任务执行失败</font>\\s" +
            "         > 空间ID:{} \\s" +
            "         > 组名称:{} \\s" +
            "         > 任务名称:{} \\s" +
            "         > 执行器名称:{} \\s" +
            "         > 通知场景:{} \\s" +
            "         > 失败原因:{} \\s" +
            "         > 方法参数:{} \\s" +
            "         > 时间:{}";
    /**
     * job任务失败数据
     */
    private final LinkedBlockingQueue<JobTaskFailAlarmEventDTO> queue = new LinkedBlockingQueue<>(1000);

    protected JobTaskFailAlarmListener(JobTaskBatchDao jobTaskBatchDao) {
        super(jobTaskBatchDao);
    }

    @Override
    protected List<JobAlarmInfo> poll() throws InterruptedException {
        // 无数据时阻塞线程
        JobTaskFailAlarmEventDTO jobTaskFailAlarmEventDTO = queue.poll(100, TimeUnit.MILLISECONDS);
        if (Objects.isNull(jobTaskFailAlarmEventDTO)) {
            return Lists.newArrayList();
        }

        // 拉取200条
        ArrayList<JobTaskFailAlarmEventDTO> lists = Lists.newArrayList(jobTaskFailAlarmEventDTO);
        queue.drainTo(lists, 200);

        // 数据类型转换
        return JobTaskConverter.INSTANCE.toJobTaskFailAlarmEventDTO(lists);
    }

    @Override
    protected AlarmContext buildAlarmContext(JobAlarmInfo alarmDTO, NotifyConfigInfo notifyConfig) {

        // 预警
        return AlarmContext.build()
                .text(MESSAGES_FORMATTER,
                        EnvironmentUtils.getActiveProfile(),
                        alarmDTO.getNamespaceId(),
                        alarmDTO.getGroupName(),
                        alarmDTO.getJobName(),
                        alarmDTO.getExecutorInfo(),
                        EnumValueFactory.getRequired(JobNotifyScene.class, alarmDTO.getNotifyScene()).getDescription(),
                        alarmDTO.getReason(),
                        alarmDTO.getArgsStr(),
                        DateUtils.toNowFormat(DateUtils.NORM_DATETIME_PATTERN))
                .title("{}环境 JOB任务失败", EnvironmentUtils.getActiveProfile());
    }

    @Override
    protected void startLog() {
        SilenceJobLog.LOCAL.info("JobTaskFailAlarmListener started");
    }

    @Override
    protected int getNotifyScene() {
        return JobNotifyScene.JOB_TASK_ERROR.getValue().intValue();
    }

    @Override
    protected List<SystemTaskType> getSystemTaskType() {
        return Lists.newArrayList(SystemTaskType.JOB);
    }

    @Override
    @TransactionalEventListener(fallbackExecution = true, phase = TransactionPhase.AFTER_COMPLETION)
    public void doOnApplicationEvent(JobTaskFailAlarmEvent jobTaskFailAlarmEvent) {
        if (!queue.offer(jobTaskFailAlarmEvent.getJobTaskFailAlarmEventDTO())) {
            SilenceJobLog.LOCAL.warn("JOB任务执行失败告警队列已满");
        }
    }
}
