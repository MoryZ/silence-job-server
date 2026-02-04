package com.old.silence.job.server.retry.task.support.generator.retry;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.core.enums.EnumValueFactory;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.BackoffType;
import com.old.silence.job.common.enums.DelayLevelEnum;
import com.old.silence.job.common.enums.RetryStatus;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.WaitStrategy;
import com.old.silence.job.server.common.config.SystemProperties;
import com.old.silence.job.server.common.strategy.WaitStrategies;
import com.old.silence.job.server.common.strategy.WaitStrategies.WaitStrategyContext;
import com.old.silence.job.server.common.strategy.WaitStrategies.WaitStrategyEnum;
import com.old.silence.job.server.domain.model.GroupConfig;
import com.old.silence.job.server.domain.model.Retry;
import com.old.silence.job.server.domain.model.RetrySceneConfig;
import com.old.silence.job.server.domain.model.RetryTask;
import com.old.silence.job.server.domain.service.AccessTemplate;
import com.old.silence.job.server.domain.service.task.TaskAccess;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;
import com.old.silence.job.server.retry.task.support.RetryTaskLogConverter;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;



public abstract class AbstractGenerator implements TaskGenerator {

    protected final AccessTemplate accessTemplate;
    private final SystemProperties systemProperties;

    protected AbstractGenerator(AccessTemplate accessTemplate, SystemProperties systemProperties) {
        this.accessTemplate = accessTemplate;
        this.systemProperties = systemProperties;
    }

    @Override
    public void taskGenerator(TaskContext taskContext) {
        SilenceJobLog.LOCAL.debug("received report data. {}", JSON.toJSONString(taskContext));

        RetrySceneConfig retrySceneConfig = checkAndInitScene(taskContext);

        //客户端上报任务根据幂等id去重
        List<TaskContext.TaskInfo> taskInfos = taskContext.getTaskInfos().stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(TaskContext.TaskInfo::getIdempotentId))),
                ArrayList::new));

        Set<String> idempotentIdSet = StreamUtils.toSet(taskInfos, TaskContext.TaskInfo::getIdempotentId);

        TaskAccess<Retry> retryTaskAccess = accessTemplate.getRetryAccess();

        // 获取相关的任务，用户幂等校验
        List<Retry> retries = retryTaskAccess.list(new LambdaQueryWrapper<Retry>()
                        .eq(Retry::getNamespaceId, taskContext.getNamespaceId())
                        .eq(Retry::getGroupName, taskContext.getGroupName())
                        .eq(Retry::getSceneName, taskContext.getSceneName())
                        .eq(Retry::getRetryStatus, RetryStatus.RUNNING)
                        .eq(Retry::getTaskType, SystemTaskType.RETRY)
                        .in(Retry::getIdempotentId, idempotentIdSet));

        Map<String/*幂等ID*/, List<Retry>> retryTaskMap = StreamUtils.groupByKey(retries, Retry::getIdempotentId);

        List<Retry> waitInsertTasks = new ArrayList<>();
        Instant now = Instant.now();
        for (TaskContext.TaskInfo taskInfo : taskInfos) {
            Pair<List<Retry>, List<RetryTask>> pair = doConvertTask(retryTaskMap, taskContext, now, taskInfo,
                    retrySceneConfig);
            waitInsertTasks.addAll(pair.getKey());
        }

        if (CollectionUtils.isEmpty(waitInsertTasks)) {
            return;
        }

        Assert.isTrue(
                waitInsertTasks.size() == retryTaskAccess.insertBatch(waitInsertTasks),
                () -> new SilenceJobServerException("failed to report data"));
    }

    private Pair<List<Retry>, List<RetryTask>> doConvertTask(Map<String/*幂等ID*/, List<Retry>> retryTaskMap,
                                                             TaskContext taskContext, Instant now,
                                                             TaskContext.TaskInfo taskInfo, RetrySceneConfig retrySceneConfig) {
        List<Retry> waitInsertRetryList = new ArrayList<>();
        List<RetryTask> waitInsertTaskList = new ArrayList<>();

        // 判断是否存在与幂等ID相同的任务
        List<Retry> list = retryTaskMap.getOrDefault(taskInfo.getIdempotentId(), new ArrayList<>()).stream()
                .filter(retryTask ->
                        taskContext.getGroupName().equals(retryTask.getGroupName())
                                && taskContext.getNamespaceId().equals(retryTask.getNamespaceId())
                                && taskContext.getSceneName().equals(retryTask.getSceneName())).collect(Collectors.toList());
        // 说明存在相同的任务
        if (CollectionUtils.isNotEmpty(list)) {
            SilenceJobLog.LOCAL.warn("interrupted reporting in retrying task. [{}]", JSON.toJSONString(taskInfo));
            return Pair.of(waitInsertRetryList, waitInsertTaskList);
        }

        Retry retry = RetryTaskConverter.INSTANCE.toRetryTask(taskInfo);
        retry.setNamespaceId(taskContext.getNamespaceId());
        retry.setTaskType(SystemTaskType.RETRY);
        retry.setGroupName(taskContext.getGroupName());
        retry.setSceneName(taskContext.getSceneName());
        retry.setRetryStatus(initStatus(taskContext));
        retry.setParentId(BigInteger.ZERO);
        retry.setDeleted(false);
        if (StrUtil.isBlank(retry.getBizNo())) {
            // 默认生成一个业务单据号方便用户查询
            retry.setBizNo(IdUtil.fastSimpleUUID());
        } else {
            retry.setBizNo(retry.getBizNo());
        }

        // 计算分桶逻辑
        retry.setBucketIndex(
                HashUtil.bkdrHash(taskContext.getGroupName() + taskContext.getSceneName() + taskInfo.getIdempotentId())
                        % systemProperties.getBucketTotal()
        );

        retry.setCreatedDate(now);
        retry.setUpdatedDate(now);

        if (StrUtil.isBlank(retry.getExtAttrs())) {
            retry.setExtAttrs(StrUtil.EMPTY);
        }

        WaitStrategyContext waitStrategyContext = new WaitStrategyContext();
        waitStrategyContext.setNextTriggerAt(now);
        waitStrategyContext.setTriggerInterval(retrySceneConfig.getTriggerInterval());
        waitStrategyContext.setDelayLevel(1);
        WaitStrategy waitStrategy = WaitStrategyEnum.getWaitStrategy(retrySceneConfig.getBackOff().getValue());
        retry.setNextTriggerAt(waitStrategy.computeTriggerTime(waitStrategyContext));
        waitInsertRetryList.add(retry);

        RetryTask retryTask = RetryTaskLogConverter.INSTANCE.toRetryTask(retry);
        retryTask.setTaskType(SystemTaskType.RETRY);
        retryTask.setCreatedDate(now);
        waitInsertTaskList.add(retryTask);

        return Pair.of(waitInsertRetryList, waitInsertTaskList);
    }

    protected abstract RetryStatus initStatus(TaskContext taskContext);

    private RetrySceneConfig checkAndInitScene(TaskContext taskContext) {
        RetrySceneConfig retrySceneConfig = accessTemplate.getSceneConfigAccess()
                .getSceneConfigByGroupNameAndSceneName(taskContext.getGroupName(), taskContext.getSceneName(),
                        taskContext.getNamespaceId());
        if (Objects.isNull(retrySceneConfig)) {

            GroupConfig groupConfig = accessTemplate.getGroupConfigAccess()
                    .getGroupConfigByGroupName(taskContext.getGroupName(), taskContext.getNamespaceId());
            if (Objects.isNull(groupConfig)) {
                throw new SilenceJobServerException(
                        "failed to report data, no group configuration found. groupName:[{}]", taskContext.getGroupName());
            }

            if (groupConfig.getInitScene().equals(500)) {
                throw new SilenceJobServerException(
                        "failed to report data, no scene configuration found. groupName:[{}] sceneName:[{}]",
                        taskContext.getGroupName(), taskContext.getSceneName());
            } else {
                // 若配置了默认初始化场景配置，则发现上报数据的时候未配置场景，默认生成一个场景
                retrySceneConfig = initScene(taskContext.getGroupName(), taskContext.getSceneName(), taskContext.getNamespaceId());
            }
        }

        return retrySceneConfig;

    }

    /**
     * 若配置了默认初始化场景配置，则发现上报数据的时候未配置场景，默认生成一个场景 backOff(退避策略): 等级策略 maxRetryCount(最大重试次数): 26 triggerInterval(间隔时间): see:
     * {@link DelayLevelEnum}
     *
     * @param groupName 组名称
     * @param sceneName 场景名称
     */
    private RetrySceneConfig initScene(String groupName, String sceneName, String namespaceId) {
        RetrySceneConfig retrySceneConfig = new RetrySceneConfig();
        retrySceneConfig.setNamespaceId(namespaceId);
        retrySceneConfig.setGroupName(groupName);
        retrySceneConfig.setSceneName(sceneName);
        retrySceneConfig.setSceneStatus(true);
        retrySceneConfig.setBackOff(EnumValueFactory.getRequired(BackoffType.class, WaitStrategies.WaitStrategyEnum.DELAY_LEVEL.getValue()));
        retrySceneConfig.setMaxRetryCount(DelayLevelEnum._21.getLevel());
        retrySceneConfig.setCbStatus(false);
        retrySceneConfig.setCbMaxCount(DelayLevelEnum._16.getLevel());
        retrySceneConfig.setDescription("自动初始化场景");
        Assert.isTrue(1 == accessTemplate.getSceneConfigAccess().insert(retrySceneConfig),
                () -> new SilenceJobServerException("init scene error"));
        return retrySceneConfig;
    }

}
