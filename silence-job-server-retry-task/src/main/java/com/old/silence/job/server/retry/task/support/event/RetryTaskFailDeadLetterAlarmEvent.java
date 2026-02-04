package com.old.silence.job.server.retry.task.support.event;

import org.springframework.context.ApplicationEvent;
import com.old.silence.job.server.retry.task.dto.RetryTaskFailDeadLetterAlarmEventDTO;

import java.util.List;

/**
 * 重试任务失败进入死信队列事件
 *
 */

public class RetryTaskFailDeadLetterAlarmEvent extends ApplicationEvent {

    private List<RetryTaskFailDeadLetterAlarmEventDTO> retryDeadLetters;

    public RetryTaskFailDeadLetterAlarmEvent(List<RetryTaskFailDeadLetterAlarmEventDTO> retryDeadLetters) {
        super(retryDeadLetters);
        this.retryDeadLetters = retryDeadLetters;
    }

    public List<RetryTaskFailDeadLetterAlarmEventDTO> getRetryDeadLetters() {
        return retryDeadLetters;
    }

    public void setRetryDeadLetters(List<RetryTaskFailDeadLetterAlarmEventDTO> retryDeadLetters) {
        this.retryDeadLetters = retryDeadLetters;
    }
}
