package com.old.silence.job.server.api.assembler;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import com.old.silence.core.mapstruct.MapStructSpringConfig;
import com.old.silence.job.server.domain.model.RetrySceneConfig;
import com.old.silence.job.server.domain.model.RetrySceneConfigNotifyConfigRelation;
import com.old.silence.job.server.dto.SceneConfigCommand;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring", uses = MapStructSpringConfig.class)
public interface SceneConfigMapper extends Converter<SceneConfigCommand, RetrySceneConfig> {


    @Override
    RetrySceneConfig convert(SceneConfigCommand sceneConfigCommand);

    @Mapping(target = "notifyIds", expression = "java(toNotifyIds(requestVO.getNotifyRelations()))")
    SceneConfigCommand toSceneConfigRequestVO(RetrySceneConfig requestVO);

    default Set<BigInteger> toNotifyIds(List<RetrySceneConfigNotifyConfigRelation> notifyRelations) {
        if (notifyRelations == null || notifyRelations.isEmpty()) {
            return Set.of();
        }

        return notifyRelations.stream()
                .map(RetrySceneConfigNotifyConfigRelation::getNotifyConfigId)
                .collect(Collectors.toSet());
    }
}
