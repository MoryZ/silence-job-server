package com.old.silence.job.server.retry.task.support.dispatch;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.old.silence.job.common.server.dto.RetryLogTaskDTO;
import com.old.silence.job.log.dto.TaskLogFieldDTO;
import com.old.silence.job.server.domain.model.RetryTaskLogMessage;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryTaskLogMessageDao;
import com.old.silence.job.server.common.pekko.ActorGenerator;
import com.old.silence.job.server.retry.task.dto.RetryTaskLogDTO;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;
import com.old.silence.job.server.task.common.actor.AbstractLogActor;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Retry 日志 Actor
 */
@Component(ActorGenerator.LOG_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RetryLogActor extends AbstractLogActor<RetryLogTaskDTO, RetryTaskLogDTO, RetryTaskLogMessage> {

    private final RetryTaskLogMessageDao retryTaskLogMessageDao;

    public RetryLogActor(RetryTaskLogMessageDao retryTaskLogMessageDao) {
        this.retryTaskLogMessageDao = retryTaskLogMessageDao;
    }

    @Override
    protected Class<RetryTaskLogDTO> getSingleLogClass() {
        return RetryTaskLogDTO.class;
    }

    @Override
    protected BigInteger getTaskId(RetryLogTaskDTO logTask) {
        return logTask.getRetryTaskId();
    }

    @Override
    protected List<TaskLogFieldDTO> getFieldList(RetryLogTaskDTO logTask) {
        return logTask.getFieldList();
    }

    @Override
    protected RetryTaskLogMessage createLogMessage(RetryLogTaskDTO logTask) {
        return RetryTaskConverter.INSTANCE.toRetryTaskLogMessage(logTask);
    }

    @Override
    protected RetryTaskLogMessage createSingleLogMessage(RetryTaskLogDTO logDTO) {
        RetryTaskLogMessage retryTaskLogMessage = new RetryTaskLogMessage();
        retryTaskLogMessage.setRetryId(logDTO.getRetryId());
        retryTaskLogMessage.setRetryTaskId(logDTO.getRetryTaskId());
        retryTaskLogMessage.setGroupName(logDTO.getGroupName());
        retryTaskLogMessage.setNamespaceId(logDTO.getNamespaceId());
        retryTaskLogMessage.setLogNum(1);
        retryTaskLogMessage.setRealTime(logDTO.getRealTime());
        retryTaskLogMessage.setMessage(getStringOrEmpty(logDTO.getMessage()));
        retryTaskLogMessage.setCreatedDate(Optional.ofNullable(logDTO.getTriggerTime()).orElse(Instant.now()));
        return retryTaskLogMessage;
    }

    @Override
    protected void setCreatedDate(RetryTaskLogMessage logMessage, Instant createdDate) {
        logMessage.setCreatedDate(createdDate);
    }

    @Override
    protected void setLogNum(RetryTaskLogMessage logMessage, int logNum) {
        logMessage.setLogNum(logNum);
    }

    @Override
    protected void setMessage(RetryTaskLogMessage logMessage, String message) {
        logMessage.setMessage(message);
    }

    @Override
    protected void insertBatch(List<RetryTaskLogMessage> logMessages) {
        retryTaskLogMessageDao.insertBatch(logMessages);
    }

    @Override
    protected void insert(RetryTaskLogMessage logMessage) {
        retryTaskLogMessageDao.insert(logMessage);
    }
}
