package com.old.silence.job.server.retry.task.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import com.old.silence.job.server.domain.model.RetryDeadLetter;
import com.old.silence.job.server.retry.task.dto.RetryPartitionTask;


@Mapper
public interface RetryDeadLetterConverter {

    RetryDeadLetterConverter INSTANCE = Mappers.getMapper(RetryDeadLetterConverter.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    RetryDeadLetter toRetryDeadLetter(RetryPartitionTask retryTasks);

}
