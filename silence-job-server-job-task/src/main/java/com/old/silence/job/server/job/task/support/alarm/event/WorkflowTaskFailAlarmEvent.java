package com.old.silence.job.server.job.task.support.alarm.event;

import org.springframework.context.ApplicationEvent;
import com.old.silence.job.server.job.task.dto.WorkflowTaskFailAlarmEventDTO;


public class WorkflowTaskFailAlarmEvent extends ApplicationEvent {

    private final WorkflowTaskFailAlarmEventDTO workflowTaskFailAlarmEventDTO;

    public WorkflowTaskFailAlarmEvent(WorkflowTaskFailAlarmEventDTO workflowTaskFailAlarmEventDTO) {
        super(workflowTaskFailAlarmEventDTO);
        this.workflowTaskFailAlarmEventDTO = workflowTaskFailAlarmEventDTO;
    }

    public WorkflowTaskFailAlarmEventDTO getWorkflowTaskFailAlarmEventDTO() {
        return workflowTaskFailAlarmEventDTO;
    }
}
