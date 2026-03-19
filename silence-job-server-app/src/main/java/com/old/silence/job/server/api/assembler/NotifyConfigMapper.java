package com.old.silence.job.server.api.assembler;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;
import com.old.silence.core.mapstruct.MapStructSpringConfig;
import com.old.silence.job.server.domain.model.NotifyConfig;
import com.old.silence.job.server.dto.NotifyConfigCommand;

@Mapper(componentModel = "spring", uses = MapStructSpringConfig.class)
public interface NotifyConfigMapper extends Converter<NotifyConfigCommand, NotifyConfig> {

    @Override
    NotifyConfig convert(NotifyConfigCommand notifyConfigCommand);
}
