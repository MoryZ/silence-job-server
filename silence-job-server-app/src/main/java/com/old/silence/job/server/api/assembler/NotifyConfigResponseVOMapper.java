package com.old.silence.job.server.api.assembler;

import com.old.silence.core.util.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import com.old.silence.core.mapstruct.MapStructSpringConfig;
import com.old.silence.job.server.domain.model.NotifyConfig;
import com.old.silence.job.server.domain.model.NotifyConfigRecipientRelation;
import java.util.List;
import java.util.stream.Collectors;
import com.old.silence.job.server.vo.NotifyConfigResponseVO;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;


@Mapper(componentModel = "spring", uses = MapStructSpringConfig.class)
public interface NotifyConfigResponseVOMapper extends Converter<NotifyConfig, NotifyConfigResponseVO> {


    @Override
    @Mapping(target = "recipientIds", expression = "java(toNotifyRecipientIds(notifyConfig.getRecipientRelations()))")
    NotifyConfigResponseVO convert(NotifyConfig notifyConfig);

    default Set<BigInteger> toNotifyRecipientIds(List<NotifyConfigRecipientRelation> recipientRelations) {
        if (CollectionUtils.isEmpty(recipientRelations)) {
            return new HashSet<>();
        }

        return recipientRelations.stream()
            .map(NotifyConfigRecipientRelation::getRecipientId)
            .collect(Collectors.toSet());
    }
}
