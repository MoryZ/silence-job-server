package com.old.silence.job.server.api.assembler;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import com.old.silence.core.mapstruct.MapStructSpringConfig;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.domain.model.JobNotifyConfigRelation;
import com.old.silence.job.server.dto.JobCommand;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring", uses = MapStructSpringConfig.class)
public interface JobMapper extends Converter<JobCommand, Job> {

    @Override
    @Mapping(target = "notifyRelations", expression = "java(toJobNotifyConfigRelations(jobCommand.getNotifyIds()))")
    Job convert(JobCommand jobCommand);

    default List<JobNotifyConfigRelation> toJobNotifyConfigRelations(Set<BigInteger> notifyIds) {
        if (CollectionUtils.isEmpty(notifyIds)) {
            return List.of();
        }

        return notifyIds.stream().map(notifyId -> {
            var jobNotifyConfigRelation = new JobNotifyConfigRelation();
            jobNotifyConfigRelation.setNotifyConfigId(notifyId);
            return jobNotifyConfigRelation;
        }).collect(Collectors.toList());
    }
}
