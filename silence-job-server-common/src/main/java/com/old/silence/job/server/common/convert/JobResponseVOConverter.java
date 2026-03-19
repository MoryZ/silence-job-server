package com.old.silence.job.server.common.convert;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.common.vo.JobResponseVO;
import com.old.silence.job.server.domain.model.Job;

import java.math.BigInteger;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@Mapper
public interface JobResponseVOConverter {

    JobResponseVOConverter INSTANCE = Mappers.getMapper(JobResponseVOConverter.class);

    //    @Mappings({
//        @Mapping(source = "nextTriggerAt", target = "nextTriggerAt", expression = "java(DateUtils.toLocalDateTime())")
//    })
    List<JobResponseVO> convertList(List<Job> jobs);

    @Mapping(target = "nextTriggerAt", expression = "java(toLocalDateTime(job.getNextTriggerAt()))")
    @Mapping(target = "notifyIds", expression = "java(toJobNotifyIds(job.getNotifyRelations()))")
    JobResponseVO convert(Job job);

    default Instant toLocalDateTime(Long nextTriggerAt) {
        if (Objects.isNull(nextTriggerAt) || nextTriggerAt == 0) {
            return null;
        }

        return DateUtils.toLocalDateTime(nextTriggerAt);
    }

    default Set<BigInteger> toJobNotifyIds(java.util.List<com.old.silence.job.server.domain.model.JobNotifyConfigRelation> notifyRelations) {
        if (com.old.silence.core.util.CollectionUtils.isEmpty(notifyRelations)) {
            return new HashSet<>();
        }

        return notifyRelations.stream().map(r -> r.getNotifyConfigId()).collect(java.util.stream.Collectors.toSet());
    }
}
