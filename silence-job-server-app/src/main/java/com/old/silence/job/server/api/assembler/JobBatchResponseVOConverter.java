package com.old.silence.job.server.api.assembler;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import com.old.silence.core.mapstruct.MapStructSpringConfig;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.vo.JobBatchResponseDO;
import com.old.silence.job.server.vo.JobTaskBatchResponseVO;

import java.time.Instant;
import java.util.Objects;


@Mapper(componentModel = "spring", uses = MapStructSpringConfig.class)
public interface JobBatchResponseVOConverter extends Converter<JobBatchResponseDO, JobTaskBatchResponseVO> {


    @Override
    @Mapping(target = "executionAt", expression = "java(toLocalDateTime(jobBatchResponseDO.getExecutionAt()))")
    JobTaskBatchResponseVO convert(JobBatchResponseDO jobBatchResponseDO);


    @Mapping(target = "executionAt", expression = "java(toLocalDateTime(jobTaskBatch.getExecutionAt()))")
    JobTaskBatchResponseVO convert(JobTaskBatch jobTaskBatch);

    @Mapping(source = "jobBatch.groupName", target = "groupName")
    @Mapping(source = "jobBatch.id", target = "id")
    @Mapping(target = "executionAt", expression = "java(toLocalDateTime(jobBatch.getExecutionAt()))")
    @Mapping(source = "jobBatch.createdDate", target="createdDate")
    @Mapping(source = "jobBatch.updatedDate", target="updatedDate")
    @Mapping(source = "job.taskType", target="taskType")
    JobTaskBatchResponseVO convert(JobTaskBatch jobBatch, Job job);

    default Instant toLocalDateTime(Long nextTriggerAt) {
        if (Objects.isNull(nextTriggerAt) || nextTriggerAt == 0) {
            return null;
        }

        return Instant.ofEpochMilli(nextTriggerAt);
    }

}
