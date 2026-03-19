package com.old.silence.job.server.api.assembler;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import com.old.silence.core.mapstruct.MapStructSpringConfig;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.domain.model.JobNotifyConfigRelation;
import com.old.silence.job.server.vo.JobResponseVO;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring", uses = MapStructSpringConfig.class)
public interface JobResponseVOMapper extends Converter<Job, JobResponseVO> {


    @Override
    @Mapping(target = "nextTriggerAt", expression = "java(toLocalDateTime(job.getNextTriggerAt()))")
    @Mapping(target = "notifyIds", expression = "java(toNotifyIds(job.getNotifyRelations()))")
    JobResponseVO convert(Job job);


    default Set<BigInteger> toNotifyIds(List<JobNotifyConfigRelation> notifyRelations) {
        if (notifyRelations == null || notifyRelations.isEmpty()) {
            return Set.of();
        }

        return notifyRelations.stream()
                .map(JobNotifyConfigRelation::getNotifyConfigId)
                .collect(Collectors.toSet());
    }

    default Instant toLocalDateTime(Long nextTriggerAt) {
        if (Objects.isNull(nextTriggerAt) || nextTriggerAt == 0) {
            return null;
        }

        return Instant.ofEpochMilli(nextTriggerAt);
    }
}
