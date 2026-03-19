package com.old.silence.job.server.common.convert;

import cn.hutool.core.util.StrUtil;
import com.old.silence.core.util.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.server.common.dto.JobAlarmInfo;
import com.old.silence.job.server.common.dto.NotifyConfigInfo;
import com.old.silence.job.server.common.dto.WorkflowAlarmInfo;
import com.old.silence.job.server.domain.model.NotifyConfig;
import com.old.silence.job.server.domain.model.NotifyConfigRecipientRelation;
import java.util.stream.Collectors;
import com.old.silence.job.server.vo.JobBatchResponseDO;
import com.old.silence.job.server.vo.WorkflowBatchResponseDO;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Mapper
public interface AlarmInfoConverter {

    AlarmInfoConverter INSTANCE = Mappers.getMapper(AlarmInfoConverter.class);

    List<NotifyConfigInfo> retryToNotifyConfigInfos(List<NotifyConfig> notifyConfigs);

    @Mapping(target = "recipientIds", expression = "java(toNotifyRecipientIds(notifyConfig.getRecipientRelations()))")
    NotifyConfigInfo retryToNotifyConfigInfos(NotifyConfig notifyConfig);


    JobAlarmInfo toJobAlarmInfo(JobBatchResponseDO jobBatchResponseDO);

    WorkflowAlarmInfo toWorkflowAlarmInfo(WorkflowBatchResponseDO workflowBatchResponseDO);


    default Set<BigInteger> toNotifyRecipientIds(List<NotifyConfigRecipientRelation> recipientRelations) {
        if (CollectionUtils.isEmpty(recipientRelations)) {
            return Set.of();
        }

        return recipientRelations.stream()
            .map(NotifyConfigRecipientRelation::getRecipientId)
            .collect(Collectors.toSet());
    }
}
