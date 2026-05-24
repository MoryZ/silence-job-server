package com.old.silence.job.server.api.assembler;

import com.old.silence.core.util.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import com.old.silence.core.mapstruct.MapStructSpringConfig;
import com.old.silence.job.server.domain.model.NotifyConfig;

import java.util.List;

import com.old.silence.job.server.vo.NotifyConfigAndNotifyConfigRecipientConfigRelationView;
import com.old.silence.job.server.vo.NotifyConfigRecipientConfigRelationView;
import com.old.silence.job.server.vo.NotifyConfigResponseVO;

import java.math.BigInteger;
import java.util.Set;


@Mapper(componentModel = "spring", uses = MapStructSpringConfig.class)
public interface NotifyConfigResponseVOMapper extends Converter<NotifyConfig, NotifyConfigResponseVO> {


    @Mapping(target = "recipientIds", expression = "java(toNotifyRecipientIds(notifyConfig.getRecipientRelations()))")
    NotifyConfigResponseVO convert(NotifyConfigAndNotifyConfigRecipientConfigRelationView notifyConfig);

    default Set<BigInteger> toNotifyRecipientIds(List<NotifyConfigRecipientConfigRelationView> recipientRelations) {
        if (CollectionUtils.isEmpty(recipientRelations)) {
            return Set.of();
        }

        return CollectionUtils.transformToSet(recipientRelations, NotifyConfigRecipientConfigRelationView::getRecipientConfigId);
    }
}
