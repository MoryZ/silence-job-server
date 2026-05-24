package com.old.silence.job.server.api.assembler;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import com.old.silence.core.mapstruct.MapStructSpringConfig;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.server.vo.RetrySceneConfigAndRetrySceneConfigNotifyConfigRelationView;
import com.old.silence.job.server.vo.RetrySceneConfigNotifyConfigRelationView;
import com.old.silence.job.server.vo.RetrySceneConfigResponseVO;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;


@Mapper(componentModel = "spring", uses = MapStructSpringConfig.class)
public interface RetrySceneConfigResponseVOMapper extends Converter<RetrySceneConfigAndRetrySceneConfigNotifyConfigRelationView, RetrySceneConfigResponseVO> {


    @Override
    @Mapping(target = "notifyIds", expression = "java(toNotifyConfigIds(retrySceneConfig.getNotifyRelations()))")
    RetrySceneConfigResponseVO convert(RetrySceneConfigAndRetrySceneConfigNotifyConfigRelationView retrySceneConfig);

    default Set<BigInteger> toNotifyConfigIds(List<RetrySceneConfigNotifyConfigRelationView> notifyRelations) {
        return CollectionUtils.transformToSet(notifyRelations, RetrySceneConfigNotifyConfigRelationView::getNotifyConfigId);
    }
}
