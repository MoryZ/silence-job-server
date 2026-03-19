package com.old.silence.job.server.api.assembler;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;
import com.old.silence.core.mapstruct.MapStructSpringConfig;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.dto.JobCommand;


@Mapper(componentModel = "spring", uses = MapStructSpringConfig.class)
public interface JobMapper extends Converter<JobCommand, Job> {

    @Override
    Job convert(JobCommand jobCommand);
}
