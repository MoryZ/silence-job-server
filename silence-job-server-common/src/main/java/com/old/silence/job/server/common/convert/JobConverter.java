package com.old.silence.job.server.common.convert;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.old.silence.job.server.common.vo.JobRequestVO;
import com.old.silence.job.server.domain.model.Job;


@Mapper
public interface JobConverter {

    JobConverter INSTANCE = Mappers.getMapper(JobConverter.class);

    Job convert(JobRequestVO jobRequestVO);

    JobRequestVO convertList(Job job);

}
