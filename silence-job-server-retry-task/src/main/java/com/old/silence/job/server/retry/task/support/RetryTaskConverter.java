package com.old.silence.job.server.retry.task.support;

import cn.hutool.core.util.StrUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.common.client.dto.request.DispatchCallbackResultRequest;
import com.old.silence.job.common.client.dto.request.DispatchRetryRequest;
import com.old.silence.job.common.client.dto.request.DispatchRetryResultRequest;
import com.old.silence.job.common.client.dto.request.RetryCallbackRequest;
import com.old.silence.job.common.client.dto.request.StopRetryRequest;
import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.server.dto.RetryLogTaskDTO;
import com.old.silence.job.common.server.dto.RetryTaskDTO;
import com.old.silence.job.server.common.dto.JobLogMetaDTO;
import com.old.silence.job.server.common.dto.RetryAlarmInfo;
import com.old.silence.job.server.common.dto.RetryLogMetaDTO;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.domain.model.NotifyConfig;
import com.old.silence.job.server.domain.model.Retry;
import com.old.silence.job.server.domain.model.RetryDeadLetter;
import com.old.silence.job.server.domain.model.RetrySceneConfig;
import com.old.silence.job.server.domain.model.RetryTask;
import com.old.silence.job.server.domain.model.RetryTaskLogMessage;
import com.old.silence.job.server.retry.task.dto.*;
import com.old.silence.job.server.retry.task.dto.RetryTaskGeneratorDTO;
import com.old.silence.job.server.retry.task.support.block.BlockStrategyContext;
import com.old.silence.job.server.retry.task.support.generator.retry.TaskContext;
import com.old.silence.job.server.retry.task.support.result.RetryResultContext;
import com.old.silence.job.server.retry.task.support.timer.RetryTimerContext;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Mapper(imports = {DateUtils.class})
public interface RetryTaskConverter {

    RetryTaskConverter INSTANCE = Mappers.getMapper(RetryTaskConverter.class);

    @Mapping(target = "id", ignore = true)
    Retry toRetryTask(Retry retry);

    @Mapping(target = "id", ignore = true)
    Retry toRetryTask(RetryDeadLetter retryDeadLetter);

    List<Retry> toRetryTaskList(List<RetryTaskDTO> retryTaskDTOList);

    Retry toRetryTask(TaskContext.TaskInfo taskInfo);

    List<RetryPartitionTask> toRetryPartitionTasks(List<Retry> retries);

    List<RetryPartitionTask> toRetryTaskLogPartitionTasks(List<Retry> retries);

    RetryTimerContext toRetryTimerContext(RetryTaskPrepareDTO retryTaskPrepareDTO);

    RetryTimerContext toRetryTimerContext(RetryTaskGeneratorDTO retryPartitionTask);

    List<NotifyConfigDTO> toNotifyConfigDTO(List<NotifyConfig> notifyConfigs);

    List<RetrySceneConfigPartitionTask> toRetrySceneConfigPartitionTask(List<RetrySceneConfig> retrySceneConfigs);

    @Mapping(target = "notifyIds", expression = "java(toNotifyIds(retrySceneConfig.getNotifyIds()))")
    RetrySceneConfigPartitionTask toRetrySceneConfigPartitionTask(RetrySceneConfig retrySceneConfig);

    @Mapping(target = "recipientIds", expression = "java(toNotifyRecipientIds(notifyConfig.getRecipientIds()))")
    NotifyConfigDTO toNotifyConfigDTO(NotifyConfig notifyConfig);

    default Set<BigInteger> toNotifyIds(String notifyIdsStr) {
        if (StrUtil.isBlank(notifyIdsStr)) {
            return Set.of();
        }

        return new HashSet<>(JSON.parseArray(notifyIdsStr, BigInteger.class));
    }

    default Set<BigInteger> toNotifyRecipientIds(String notifyRecipientIdsStr) {
        if (StrUtil.isBlank(notifyRecipientIdsStr)) {
            return Set.of();
        }

        return new HashSet<>(JSON.parseArray(notifyRecipientIdsStr, BigInteger.class));
    }

    RetryTaskLogMessage toRetryTaskLogMessage(RetryLogTaskDTO retryLogTaskDTO);

    @Mapping(target = "timestamp", expression = "java(DateUtils.toNowMilli())")
    RetryLogMetaDTO toLogMetaDTO(Retry retry);

    @Mapping(source = "reason", target = "reason")
    @Mapping(source = "notifyScene", target = "notifyScene")
    RetryTaskExecutorDTO toRetryTaskExecutorDTO(Retry retry, String reason, JobNotifyScene notifyScene);

    @Mapping(source = "reason", target = "reason")
    @Mapping(source = "notifyScene", target = "notifyScene")
    RetryTaskFailAlarmEventDTO toRetryTaskFailAlarmEventDTO(Retry retry, String reason, JobNotifyScene notifyScene);

    RetryAlarmInfo toRetryTaskFailAlarmEventDTO(RetryTaskFailAlarmEventDTO retryTaskFailAlarmEventDTO);

    RetryTaskGeneratorDTO toRetryTaskGeneratorContext(RetryTaskPrepareDTO prepareDTO);

    RetryTask toRetryTask(RetryTaskGeneratorDTO context);

    DispatchRetryRequest toDispatchRetryRequest(RequestRetryExecutorDTO executorDTO);

    @Mapping(target = "groupName", source = "retry.groupName")
    @Mapping(target = "sceneName", source = "retry.sceneName")
    @Mapping(target = "retryId", source = "retry.id")
    @Mapping(target = "taskType", source = "retry.taskType")
    @Mapping(target = "namespaceId", source = "retry.namespaceId")
    RequestRetryExecutorDTO toRealRetryExecutorDTO(RetrySceneConfig execute, Retry retry);

    RequestRetryExecutorDTO toRealRetryExecutorDTO(TaskStopJobDTO stopJobDTO);

    RetryExecutorResultDTO toRetryExecutorResultDTO(DispatchRetryResultRequest resultDTO);

    RetryExecutorResultDTO toRetryExecutorResultDTO(DispatchCallbackResultRequest resultDTO);

    RetryExecutorResultDTO toRetryExecutorResultDTO(TaskStopJobDTO resultDTO);

    RetryExecutorResultDTO toRetryExecutorResultDTO(RequestRetryExecutorDTO resultDTO);

    RetryExecutorResultDTO toRetryExecutorResultDTO(RequestCallbackExecutorDTO resultDTO);

    RetryTaskGeneratorDTO toRetryTaskGeneratorDTO(RetryTaskPrepareDTO jobPrepareDTO);

    @Mapping(target = "operationReason", expression = "java(com.old.silence.core.enums.EnumValueFactory.getRequired(com.old.silence.job.common.enums.RetryOperationReason.class, context.getOperationReason()))")
    RetryTaskGeneratorDTO toRetryTaskGeneratorDTO(BlockStrategyContext context);

    BlockStrategyContext toBlockStrategyContext(RetryTaskPrepareDTO prepare);

    @Mapping(target = "operationReason", expression = "java(com.old.silence.core.enums.EnumValueFactory.getRequired(com.old.silence.job.common.enums.RetryOperationReason.class, context.getOperationReason()))")
    TaskStopJobDTO toTaskStopJobDTO(BlockStrategyContext context);

    TaskStopJobDTO toTaskStopJobDTO(Retry retry);

    TaskStopJobDTO toTaskStopJobDTO(RetryTaskPrepareDTO context);

    StopRetryRequest toStopRetryRequest(RequestStopRetryTaskExecutorDTO executorDTO);

    @Mapping(source = "id", target = "retryId")
    RetryTaskPrepareDTO toRetryTaskPrepareDTO(Retry retry);

    @Mapping(source = "id", target = "retryId")
    RetryTaskPrepareDTO toRetryTaskPrepareDTO(RetryPartitionTask partitionTask);

    RetryTaskExecuteDTO toRetryTaskExecuteDTO(RetryTimerContext context);

    JobLogMetaDTO toJobLogDTO(RequestRetryExecutorDTO executorDTO);

    JobLogMetaDTO toJobLogDTO(RequestCallbackExecutorDTO executorDTO);

    RetryResultContext toRetryResultContext(RetryExecutorResultDTO resultDTO);

    @Mapping(target = "groupName", source = "retry.groupName")
    @Mapping(target = "sceneName", source = "retry.sceneName")
    @Mapping(target = "retryId", source = "retry.id")
    @Mapping(target = "taskType", source = "retry.taskType")
    @Mapping(target = "namespaceId", source = "retry.namespaceId")
    RequestCallbackExecutorDTO toRequestCallbackExecutorDTO(RetrySceneConfig retrySceneConfig, Retry retry);

    RetryCallbackRequest toRetryCallbackDTO(RequestCallbackExecutorDTO executorDTO);

    List<RetryTaskFailDeadLetterAlarmEventDTO> toRetryTaskFailDeadLetterAlarmEventDTO(List<RetryDeadLetter> retryDeadLetters);

    List<RetryAlarmInfo> toRetryAlarmInfos(List<RetryTaskFailDeadLetterAlarmEventDTO> letterAlarmEventDTOS);
}
