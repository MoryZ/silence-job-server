package com.old.silence.job.server.api.assembler;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import com.old.silence.core.mapstruct.MapStructSpringConfig;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.server.domain.model.NotifyConfig;
import com.old.silence.job.server.domain.model.NotifyConfigRecipientRelation;
import com.old.silence.job.server.dto.NotifyConfigCommand;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = MapStructSpringConfig.class)
public interface NotifyConfigMapper extends Converter<NotifyConfigCommand, NotifyConfig> {

    @Override
    @Mapping(target = "recipientRelations", expression = "java(toNotifyConfigRecipientConfigRelations(notifyConfigCommand.getRecipientIds()))")
    NotifyConfig convert(NotifyConfigCommand notifyConfigCommand);

    default List<NotifyConfigRecipientRelation> toNotifyConfigRecipientConfigRelations(Set<BigInteger> recipientIds) {
        if (CollectionUtils.isEmpty(recipientIds)) {
            return List.of();
        }

        return recipientIds.stream().map(recipientId -> {
            var notifyConfigRecipientRelation = new NotifyConfigRecipientRelation();
            notifyConfigRecipientRelation.setNotifyConfigId(recipientId);
            return notifyConfigRecipientRelation;
        }).collect(Collectors.toList());
    }
}
