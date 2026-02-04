package com.old.silence.job.server.job.task.support.alarm.event;

import org.springframework.context.ApplicationEvent;
import com.old.silence.job.server.job.task.dto.JobTaskFailAlarmEventDTO;


public class JobTaskFailAlarmEvent extends ApplicationEvent {

    private JobTaskFailAlarmEventDTO jobTaskFailAlarmEventDTO;

    public JobTaskFailAlarmEvent(JobTaskFailAlarmEventDTO jobTaskFailAlarmEventDTO) {
        super(jobTaskFailAlarmEventDTO);
        this.jobTaskFailAlarmEventDTO = jobTaskFailAlarmEventDTO;
    }

    public JobTaskFailAlarmEventDTO getJobTaskFailAlarmEventDTO() {
        return jobTaskFailAlarmEventDTO;
    }

    public void setJobTaskFailAlarmEventDTO(JobTaskFailAlarmEventDTO jobTaskFailAlarmEventDTO) {
        this.jobTaskFailAlarmEventDTO = jobTaskFailAlarmEventDTO;
    }
}
