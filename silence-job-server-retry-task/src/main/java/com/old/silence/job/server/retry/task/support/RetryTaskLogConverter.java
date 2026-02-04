package com.old.silence.job.server.retry.task.support;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import com.old.silence.job.server.common.dto.RetryLogMetaDTO;
import com.old.silence.job.server.domain.model.Retry;
import com.old.silence.job.server.domain.model.RetryTask;
import com.old.silence.job.server.domain.model.RetryTaskLogMessage;
import com.old.silence.job.server.retry.task.dto.RequestCallbackExecutorDTO;
import com.old.silence.job.server.retry.task.dto.RequestRetryExecutorDTO;
import com.old.silence.job.server.retry.task.dto.RetryMergePartitionTaskDTO;
import com.old.silence.job.server.retry.task.dto.RetryTaskLogDTO;

import java.util.List;


@Mapper
public interface RetryTaskLogConverter {

    RetryTaskLogConverter INSTANCE = Mappers.getMapper(RetryTaskLogConverter.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    RetryTask toRetryTask(Retry retry);

    RetryTaskLogDTO toRetryTaskLogDTO(Retry retry);

    RetryTaskLogDTO toRetryTaskLogDTO(RequestRetryExecutorDTO retry);

    List<RetryMergePartitionTaskDTO> toRetryMergePartitionTaskDTOs(List<RetryTask> retryTaskList);

    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    RetryTaskLogMessage toRetryTaskLogMessage(RetryTaskLogMessage message);

    RetryLogMetaDTO toRetryLogMetaDTO(RequestRetryExecutorDTO executorDTO);

    RetryLogMetaDTO toRetryLogMetaDTO(RequestCallbackExecutorDTO executorDTO);
}
