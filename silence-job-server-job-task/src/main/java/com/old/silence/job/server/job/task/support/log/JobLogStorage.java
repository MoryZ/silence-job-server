package com.old.silence.job.server.job.task.support.log;

import org.apache.pekko.actor.ActorRef;
import org.springframework.stereotype.Component;
import com.old.silence.job.log.dto.LogContentDTO;
import com.old.silence.job.log.enums.LogTypeEnum;
import com.old.silence.job.server.common.dto.JobLogMetaDTO;
import com.old.silence.job.server.job.task.dto.JobLogDTO;
import com.old.silence.job.server.common.pekko.ActorGenerator;
import com.old.silence.job.server.task.common.log.AbstractLogStorage;

/**
 * Job 日志存储（使用公共抽象类）
 *
 * @author mory
 */
@Component
public class JobLogStorage extends AbstractLogStorage<JobLogDTO, JobLogMetaDTO> {

    @Override
    public LogTypeEnum logType() {
        return LogTypeEnum.JOB;
    }

    @Override
    protected JobLogDTO createLogDTO() {
        return new JobLogDTO();
    }

    @Override
    protected void populateLogDTO(JobLogDTO dto, String message, LogContentDTO logContentDTO, JobLogMetaDTO metaDTO) {
        dto.setMessage(message);
        dto.setTaskId(metaDTO.getTaskId());
        dto.setJobId(metaDTO.getJobId());
        dto.setGroupName(metaDTO.getGroupName());
        dto.setNamespaceId(metaDTO.getNamespaceId());
        dto.setTaskBatchId(metaDTO.getTaskBatchId());
        dto.setRealTime(logContentDTO.getTimeStamp());
    }

    @Override
    protected ActorRef getLogActor() {
        return ActorGenerator.jobLogActor();
    }
}
