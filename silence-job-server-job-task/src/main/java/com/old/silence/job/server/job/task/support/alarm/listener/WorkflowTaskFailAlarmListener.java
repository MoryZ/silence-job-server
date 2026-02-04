package com.old.silence.job.server.job.task.support.alarm.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import com.google.common.collect.Lists;
import com.old.silence.job.common.alarm.AlarmContext;
import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.common.util.EnvironmentUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.alarm.AbstractWorkflowAlarm;
import com.old.silence.job.server.common.dto.NotifyConfigInfo;
import com.old.silence.job.server.common.dto.WorkflowAlarmInfo;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowTaskBatchDao;
import com.old.silence.job.server.job.task.dto.WorkflowTaskFailAlarmEventDTO;
import com.old.silence.job.server.job.task.support.WorkflowTaskConverter;
import com.old.silence.job.server.job.task.support.alarm.event.WorkflowTaskFailAlarmEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


@Component
public class WorkflowTaskFailAlarmListener extends AbstractWorkflowAlarm<WorkflowTaskFailAlarmEvent> {

    private static final String MESSAGES_FORMATTER =
            "<font face=微软雅黑 color=#ff0000 size=4>{}环境 Workflow任务执行失败</font>\\s" +
                    "         > 空间ID:{} \\s" +
                    "         > 组名称:{} \\s" +
                    "         > 工作流名称:{} \\s" +
                    "         > 通知场景:{} \\s" +
                    "         > 失败原因:{} \\s" +
                    "         > 时间:{}";
    private final LinkedBlockingQueue<WorkflowTaskFailAlarmEventDTO> queue = new LinkedBlockingQueue<>(1000);

    protected WorkflowTaskFailAlarmListener(WorkflowTaskBatchDao workflowTaskBatchDao) {
        super(workflowTaskBatchDao);
    }

    @Override
    protected List<WorkflowAlarmInfo> poll() throws InterruptedException {
        // 无数据时阻塞线程
        WorkflowTaskFailAlarmEventDTO workflowTaskFailAlarmEventDTO = queue.poll(100, TimeUnit.MILLISECONDS);
        if (Objects.isNull(workflowTaskFailAlarmEventDTO)) {
            return Lists.newArrayList();
        }

        // 拉取200条
        ArrayList<WorkflowTaskFailAlarmEventDTO> lists = Lists.newArrayList(workflowTaskFailAlarmEventDTO);
        queue.drainTo(lists, 200);

        // 数据类型转换
        return WorkflowTaskConverter.INSTANCE.toWorkflowTaskFailAlarmEventDTO(lists);
    }

    @Override
    protected AlarmContext buildAlarmContext(WorkflowAlarmInfo alarmDTO, NotifyConfigInfo notifyConfig) {

        // 预警
        return AlarmContext.build()
                .text(MESSAGES_FORMATTER,
                        EnvironmentUtils.getActiveProfile(),
                        alarmDTO.getNamespaceId(),
                        alarmDTO.getGroupName(),
                        alarmDTO.getWorkflowName(),
                        alarmDTO.getOperationReason().getDescription(),
                        alarmDTO.getReason(),
                        DateUtils.toNowFormat(DateUtils.NORM_DATETIME_PATTERN))
                .title("{}环境 Workflow任务执行失败", EnvironmentUtils.getActiveProfile());
    }

    @Override
    protected void startLog() {
        SilenceJobLog.LOCAL.info("WorkflowTaskFailAlarmListener started");
    }

    @Override
    protected int getNotifyScene() {
        return JobNotifyScene.WORKFLOW_TASK_ERROR.getValue().intValue();
    }

    @Override
    protected List<SystemTaskType> getSystemTaskType() {
        return Lists.newArrayList(SystemTaskType.WORKFLOW);
    }

    @Override
    @TransactionalEventListener(fallbackExecution = true, phase = TransactionPhase.AFTER_COMPLETION)
    public void doOnApplicationEvent(WorkflowTaskFailAlarmEvent event) {
        if (!queue.offer(event.getWorkflowTaskFailAlarmEventDTO())) {
            SilenceJobLog.LOCAL.warn("Workflow任务执行失败告警队列已满");
        }
    }
}
