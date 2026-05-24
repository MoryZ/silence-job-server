package com.old.silence.job.server.api.assembler;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import com.old.silence.core.mapstruct.MapStructSpringConfig;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.vo.JobBatchResponseDO;
import com.old.silence.job.server.vo.JobTaskBatchAndJobView;
import com.old.silence.job.server.vo.JobTaskBatchResponseVO;
import com.old.silence.job.server.vo.JobView;

import java.time.Instant;
import java.util.Objects;


@Mapper(componentModel = "spring", uses = MapStructSpringConfig.class)
public interface JobBatchResponseVOConverter extends Converter<JobBatchResponseDO, JobTaskBatchResponseVO> {


    @Override
    @Mapping(target = "executionAt", expression = "java(toLocalDateTime(jobBatchResponseDO.getExecutionAt()))")
    JobTaskBatchResponseVO convert(JobBatchResponseDO jobBatchResponseDO);


    @Mapping(target = "executionAt", expression = "java(toLocalDateTime(jobTaskBatch.getExecutionAt()))")
    JobTaskBatchResponseVO convert(JobTaskBatch jobTaskBatch);

    @Mapping(source = "groupName", target = "groupName")
    @Mapping(source = "id", target = "id")
    @Mapping(target = "executionAt", expression = "java(toLocalDateTime(jobTaskBatch.getExecutionAt()))")
    @Mapping(source = "job.taskType", target="taskType")
    @Mapping(source = "job.jobName", target="jobName")
    @Mapping(source = "job.executorType", target="executorType")
    @Mapping(source = "job.executorInfo", target="executorInfo")
    JobTaskBatchResponseVO convert(JobTaskBatchAndJobView jobTaskBatch);

    default Instant toLocalDateTime(Long nextTriggerAt) {
        if (Objects.isNull(nextTriggerAt) || nextTriggerAt == 0) {
            return null;
        }

        return Instant.ofEpochMilli(nextTriggerAt);
    }

}
