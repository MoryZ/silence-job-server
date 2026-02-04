package com.old.silence.job.server.retry.task.support.log;

import org.apache.pekko.actor.ActorRef;
import org.springframework.stereotype.Component;
import com.old.silence.job.log.dto.LogContentDTO;
import com.old.silence.job.log.enums.LogTypeEnum;
import com.old.silence.job.server.common.dto.RetryLogMetaDTO;
import com.old.silence.job.server.common.pekko.ActorGenerator;
import com.old.silence.job.server.retry.task.dto.RetryTaskLogDTO;
import com.old.silence.job.server.task.common.log.AbstractLogStorage;

/**
 * Retry 日志存储（使用公共抽象类）
 *
 * @author mory
 */
@Component
public class RetryLogStorage extends AbstractLogStorage<RetryTaskLogDTO, RetryLogMetaDTO> {

    @Override
    public LogTypeEnum logType() {
        return LogTypeEnum.RETRY;
    }

    @Override
    protected RetryTaskLogDTO createLogDTO() {
        return new RetryTaskLogDTO();
    }

    @Override
    protected void populateLogDTO(RetryTaskLogDTO dto, String message, LogContentDTO logContentDTO, RetryLogMetaDTO metaDTO) {
        dto.setMessage(message);
        dto.setGroupName(metaDTO.getGroupName());
        dto.setNamespaceId(metaDTO.getNamespaceId());
        dto.setRetryId(metaDTO.getRetryId());
        dto.setRetryTaskId(metaDTO.getRetryTaskId());
        dto.setRealTime(metaDTO.getTimestamp());
    }

    @Override
    protected ActorRef getLogActor() {
        return ActorGenerator.logActor();
    }
}
