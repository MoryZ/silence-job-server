package com.old.silence.job.server.retry.task.service;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.old.silence.job.common.server.dto.RetryTaskDTO;
import com.old.silence.job.server.retry.task.support.generator.retry.TaskContext;

@Mapper
public interface TaskContextConverter {
    TaskContextConverter INSTANCE = Mappers.getMapper(TaskContextConverter.class);

    TaskContext.TaskInfo toTaskContextInfo(RetryTaskDTO retryTask);
}
