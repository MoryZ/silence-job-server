package com.old.silence.job.server.retry.task.support.dispatch;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.enums.RetryStatus;
import com.old.silence.job.common.enums.RetryTaskExecutorSceneEnum;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.WaitStrategy;
import com.old.silence.job.server.common.config.SystemProperties;
import com.old.silence.job.server.common.dto.PartitionTask;
import com.old.silence.job.server.common.dto.ScanTask;
import com.old.silence.job.server.common.strategy.WaitStrategies;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.common.util.PartitionTaskUtils;
import com.old.silence.job.server.domain.model.GroupConfig;
import com.old.silence.job.server.domain.model.Retry;
import com.old.silence.job.server.domain.model.RetrySceneConfig;
import com.old.silence.job.server.infrastructure.persistence.dao.GroupConfigDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetrySceneConfigDao;
import com.old.silence.job.server.common.pekko.ActorGenerator;
import com.old.silence.job.server.retry.task.dto.RetryPartitionTask;
import com.old.silence.job.server.retry.task.dto.RetryTaskPrepareDTO;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;
import com.old.silence.job.server.retry.task.support.handler.RateLimiterHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Component(ActorGenerator.SCAN_RETRY_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)

public class ScanRetryActor extends AbstractActor {


    private static final Logger log = LoggerFactory.getLogger(ScanRetryActor.class);
    private final SystemProperties systemProperties;
    private final RetrySceneConfigDao retrySceneConfigDao;
    private final RetryDao retryDao;
    private final GroupConfigDao groupConfigDao;
    private final RateLimiterHandler rateLimiterHandler;

    public ScanRetryActor(SystemProperties systemProperties, RetrySceneConfigDao retrySceneConfigDao,
                          RetryDao retryDao, GroupConfigDao groupConfigDao,
                          RateLimiterHandler rateLimiterHandler) {
        this.systemProperties = systemProperties;
        this.retrySceneConfigDao = retrySceneConfigDao;
        this.retryDao = retryDao;
        this.groupConfigDao = groupConfigDao;
        this.rateLimiterHandler = rateLimiterHandler;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ScanTask.class, config -> {

            try {
                doScan(config);
            } catch (Exception e) {
                SilenceJobLog.LOCAL.error("Data scanner processing exception. [{}]", config, e);
            }

        }).build();

    }

    private void doScan(ScanTask scanTask) {
        PartitionTaskUtils.process(startId -> listAvailableTasks(startId, scanTask.getBuckets()),
                this::processRetryPartitionTasks, this::stopCondition, 0);
    }

    /**
     * 拉取任务停止判断
     *
     * @param partitionTasks RetryPartitionTask
     * @return true-停止拉取 false-继续拉取
     */
    private boolean stopCondition(List<? extends PartitionTask> partitionTasks) {
        if (CollectionUtils.isEmpty(partitionTasks)) {
            return true;
        }

        if (!rateLimiterHandler.tryAcquire(partitionTasks.size())) {
            log.warn("当前节点触发限流");
            return true;
        }

        return false;
    }

    private void processRetryPartitionTasks(List<? extends PartitionTask> partitionTasks) {
        if (CollectionUtils.isEmpty(partitionTasks)) {
            return;
        }

        // 批次查询场景
        Map<String, RetrySceneConfig> sceneConfigMap = getSceneConfigMap(partitionTasks);

        List<Retry> waitUpdateRetries = new ArrayList<>();
        List<RetryTaskPrepareDTO> waitExecRetries = new ArrayList<>();
        for (PartitionTask task : partitionTasks) {
            RetryPartitionTask retryPartitionTask = (RetryPartitionTask) task;
            RetrySceneConfig retrySceneConfig = sceneConfigMap.get(retryPartitionTask.getSceneName());
            if (Objects.isNull(retrySceneConfig)) {
                continue;
            }

            processRetry(retryPartitionTask, retrySceneConfig, waitExecRetries, waitUpdateRetries);

        }

        if (CollectionUtils.isEmpty(waitUpdateRetries)) {
            return;
        }

        // 批量更新
        retryDao.updateBatchNextTriggerAtById(waitUpdateRetries);

        for (RetryTaskPrepareDTO retryTaskPrepareDTO: waitExecRetries) {
            // 准备阶段执行
            ActorRef actorRef = ActorGenerator.retryTaskPrepareActor();
            actorRef.tell(retryTaskPrepareDTO, actorRef);
        }

    }

    /**
     * 查询场景配置或者退避策略
     *
     * @param partitionTasks 待处理任务列表
     * @return <SceneName, RetrySceneConfig>
     */
    private Map<String, RetrySceneConfig> getSceneConfigMap(List<? extends PartitionTask> partitionTasks) {
        Set<String> sceneNameSet = StreamUtils.toSet(partitionTasks,
                partitionTask -> ((RetryPartitionTask) partitionTask).getSceneName());
        List<RetrySceneConfig> retrySceneConfigs = retrySceneConfigDao
                .selectList(new LambdaQueryWrapper<RetrySceneConfig>()
                        .select(RetrySceneConfig::getBackOff, RetrySceneConfig::getTriggerInterval,
                                RetrySceneConfig::getBlockStrategy, RetrySceneConfig::getSceneName,
                                RetrySceneConfig::getCbTriggerType, RetrySceneConfig::getCbTriggerInterval,
                                RetrySceneConfig::getExecutorTimeout)
                        .eq(RetrySceneConfig::getSceneStatus, true)
                        .in(RetrySceneConfig::getSceneName, sceneNameSet));
        return StreamUtils.toIdentityMap(retrySceneConfigs, RetrySceneConfig::getSceneName);
    }

    private void processRetry(RetryPartitionTask partitionTask, RetrySceneConfig retrySceneConfig, List<RetryTaskPrepareDTO> waitExecRetries, List<Retry> waitUpdateRetries) {
        Retry retry = new Retry();
        retry.setNextTriggerAt(calculateNextTriggerTime(partitionTask, retrySceneConfig));
        retry.setId(partitionTask.getId());
        waitUpdateRetries.add(retry);

        RetryTaskPrepareDTO retryTaskPrepareDTO = RetryTaskConverter.INSTANCE.toRetryTaskPrepareDTO(partitionTask);
        retryTaskPrepareDTO.setBlockStrategy(retrySceneConfig.getBlockStrategy());
        retryTaskPrepareDTO.setExecutorTimeout(retrySceneConfig.getExecutorTimeout());
        retryTaskPrepareDTO.setRetryTaskExecutorScene(RetryTaskExecutorSceneEnum.AUTO_RETRY.getScene());
        waitExecRetries.add(retryTaskPrepareDTO);
    }

    protected Long calculateNextTriggerTime(RetryPartitionTask partitionTask, RetrySceneConfig retrySceneConfig) {
        // 更新下次触发时间

        WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();

        long now = DateUtils.toNowMilli();
        long nextTriggerAt = partitionTask.getNextTriggerAt();
        if ((nextTriggerAt + DateUtils.toEpochMilli(SystemConstants.SCHEDULE_PERIOD)) < now) {
            nextTriggerAt = now;
            partitionTask.setNextTriggerAt(nextTriggerAt);
        }

        waitStrategyContext.setNextTriggerAt(nextTriggerAt);
        waitStrategyContext.setDelayLevel(partitionTask.getRetryCount() + 1);

        // 更新触发时间, 任务进入时间轮
        WaitStrategy waitStrategy;
        if (SystemTaskType.CALLBACK.equals(partitionTask.getTaskType())) {
            waitStrategyContext.setTriggerInterval(retrySceneConfig.getCbTriggerInterval());
            waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(retrySceneConfig.getCbTriggerType().getValue());
        } else {
            waitStrategyContext.setTriggerInterval(retrySceneConfig.getTriggerInterval());
            waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(retrySceneConfig.getBackOff().getValue());
        }

        return waitStrategy.computeTriggerTime(waitStrategyContext);
    }

    public List<RetryPartitionTask> listAvailableTasks(Long startId, Set<Integer> buckets) {
        List<Retry> retries = retryDao.selectPage(
                new PageDTO<>(0, systemProperties.getRetryPullPageSize()),
                new LambdaQueryWrapper<Retry>()
                        .select(Retry::getId, Retry::getNextTriggerAt, Retry::getGroupName, Retry::getRetryCount,
                                Retry::getSceneName, Retry::getNamespaceId, Retry::getTaskType)
                        .eq(Retry::getRetryStatus, RetryStatus.RUNNING.getValue())
                        .in(Retry::getBucketIndex, buckets)
                        .le(Retry::getNextTriggerAt, DateUtils.toNowMilli() + DateUtils.toEpochMilli(SystemConstants.SCHEDULE_PERIOD))
                        .gt(Retry::getId, startId)
                        .orderByAsc(Retry::getId))
                .getRecords();

        // 过滤已关闭的组
        if (CollectionUtils.isNotEmpty(retries)) {
            List<String> groupConfigs = StreamUtils.toList(groupConfigDao.selectList(new LambdaQueryWrapper<GroupConfig>()
                            .select(GroupConfig::getGroupName)
                            .eq(GroupConfig::getGroupStatus, true)
                            .in(GroupConfig::getGroupName, StreamUtils.toSet(retries, Retry::getGroupName))),
                    GroupConfig::getGroupName);
            retries = retries.stream().filter(retry -> groupConfigs.contains(retry.getGroupName())).collect(Collectors.toList());
        }

        return RetryTaskConverter.INSTANCE.toRetryPartitionTasks(retries);
    }
}
