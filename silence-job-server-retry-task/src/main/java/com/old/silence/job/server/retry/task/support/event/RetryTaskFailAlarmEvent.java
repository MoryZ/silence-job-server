package com.old.silence.job.server.retry.task.support.event;

import org.springframework.context.ApplicationEvent;
import com.old.silence.job.server.retry.task.dto.RetryTaskFailAlarmEventDTO;

/**
 * 重试任务失败事件
 *
 */

public class RetryTaskFailAlarmEvent extends ApplicationEvent {

    private final RetryTaskFailAlarmEventDTO retryTaskFailAlarmEventDTO;

    public RetryTaskFailAlarmEvent(RetryTaskFailAlarmEventDTO retryTaskFailAlarmEventDTO) {
        super(retryTaskFailAlarmEventDTO);
        this.retryTaskFailAlarmEventDTO = retryTaskFailAlarmEventDTO;
    }

    public RetryTaskFailAlarmEventDTO getRetryTaskFailAlarmEventDTO() {
        return retryTaskFailAlarmEventDTO;
    }
}
