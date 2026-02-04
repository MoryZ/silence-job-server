package com.old.silence.job.server.job.task.support.dispatch;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.old.silence.job.common.dto.JobLogTaskDTO;
import com.old.silence.job.log.dto.TaskLogFieldDTO;
import com.old.silence.job.server.domain.model.JobLogMessage;
import com.old.silence.job.server.infrastructure.persistence.dao.JobLogMessageDao;
import com.old.silence.job.server.job.task.dto.JobLogDTO;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.common.pekko.ActorGenerator;
import com.old.silence.job.server.task.common.actor.AbstractLogActor;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Job 日志 Actor
 */
@Component(ActorGenerator.JOB_LOG_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JobLogActor extends AbstractLogActor<JobLogTaskDTO, JobLogDTO, JobLogMessage> {

    private final JobLogMessageDao jobLogMessageDao;

    public JobLogActor(JobLogMessageDao jobLogMessageDao) {
        this.jobLogMessageDao = jobLogMessageDao;
    }

    @Override
    protected Class<JobLogDTO> getSingleLogClass() {
        return JobLogDTO.class;
    }

    @Override
    protected BigInteger getTaskId(JobLogTaskDTO logTask) {
        return logTask.getTaskId();
    }

    @Override
    protected List<TaskLogFieldDTO> getFieldList(JobLogTaskDTO logTask) {
        return logTask.getFieldList();
    }

    @Override
    protected JobLogMessage createLogMessage(JobLogTaskDTO logTask) {
        return JobTaskConverter.INSTANCE.toJobLogMessage(logTask);
    }

    @Override
    protected JobLogMessage createSingleLogMessage(JobLogDTO logDTO) {
        JobLogMessage jobLogMessage = JobTaskConverter.INSTANCE.toJobLogMessage(logDTO);
        jobLogMessage.setCreatedDate(Instant.now());
        jobLogMessage.setLogNum(1);
        jobLogMessage.setMessage(getStringOrEmpty(logDTO.getMessage()));
        jobLogMessage.setTaskId(getBigIntegerOrZero(jobLogMessage.getTaskId()));
        return jobLogMessage;
    }

    @Override
    protected void setCreatedDate(JobLogMessage logMessage, Instant createdDate) {
        logMessage.setCreatedDate(createdDate);
    }

    @Override
    protected void setLogNum(JobLogMessage logMessage, int logNum) {
        logMessage.setLogNum(logNum);
    }

    @Override
    protected void setMessage(JobLogMessage logMessage, String message) {
        logMessage.setMessage(message);
    }

    @Override
    protected void insertBatch(List<JobLogMessage> logMessages) {
        jobLogMessageDao.insertBatch(logMessages);
    }

    @Override
    protected void insert(JobLogMessage logMessage) {
        jobLogMessageDao.insert(logMessage);
    }
}
