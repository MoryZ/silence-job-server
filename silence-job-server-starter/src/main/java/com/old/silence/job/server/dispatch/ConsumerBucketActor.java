package com.old.silence.job.server.dispatch;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.google.common.collect.Lists;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.cache.CacheGroupScanActor;
import com.old.silence.job.server.common.config.SystemProperties;
import com.old.silence.job.server.common.dto.ScanTask;
import com.old.silence.job.server.common.pekko.ActorGenerator;
import com.old.silence.job.server.retry.task.support.handler.RateLimiterHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 消费当前节点分配的bucket并生成扫描任务
 * <p>
 *
 */
@Component(ActorGenerator.SCAN_BUCKET_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ConsumerBucketActor extends AbstractActor {
    private static final String DEFAULT_JOB_KEY = "DEFAULT_JOB_KEY";
    private static final String DEFAULT_WORKFLOW_KEY = "DEFAULT_JOB_KEY";
    private final SystemProperties systemProperties;
    private final RateLimiterHandler rateLimiterHandler;

    private static final Map<SystemTaskType, Supplier<ActorRef>> ACTORREF_MAP = Map.of(
            SystemTaskType.RETRY, ActorGenerator::scanRetryActor,
            SystemTaskType.CALLBACK, ActorGenerator::scanCallbackGroupActor,
            SystemTaskType.JOB, ActorGenerator::scanJobActor,
            SystemTaskType.WORKFLOW, ActorGenerator::scanWorkflowActor
    );

    public ConsumerBucketActor(SystemProperties systemProperties, RateLimiterHandler rateLimiterHandler) {
        this.systemProperties = systemProperties;
        this.rateLimiterHandler = rateLimiterHandler;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ConsumerBucket.class, consumerBucket -> {

            try {
                doDispatch(consumerBucket);
            } catch (Exception e) {
                SilenceJobLog.LOCAL.error("Data dispatcher processing exception. [{}]", consumerBucket, e);
            }

        }).build();
    }

    private void doDispatch(ConsumerBucket consumerBucket) {
        if (CollectionUtils.isEmpty(consumerBucket.getBuckets())) {
            return;
        }

        // 扫描job && workflow
        doScanJobAndWorkflow(consumerBucket);

        // 扫描重试
        doScanRetry(consumerBucket);
    }

    private void doScanRetry(final ConsumerBucket consumerBucket) {

        // 刷新最新的配置
        rateLimiterHandler.refreshRate();

        // 通过并行度配置计算拉取范围
        Set<Integer> totalBuckets = consumerBucket.getBuckets();
        int retryMaxPullParallel = systemProperties.getRetryMaxPullParallel();
        List<List<Integer>> partitions = Lists.partition(new ArrayList<>(totalBuckets),
                (totalBuckets.size() + retryMaxPullParallel - 1) / retryMaxPullParallel);
        for (List<Integer> buckets : partitions) {
            ScanTask scanTask = new ScanTask();
            scanTask.setBuckets(new HashSet<>(buckets));
            ActorRef scanRetryActorRef = ActorGenerator.scanRetryActor();
            scanRetryActorRef.tell(scanTask, scanRetryActorRef);
        }
    }

    private void doScanJobAndWorkflow(final ConsumerBucket consumerBucket) {
        ScanTask scanTask = new ScanTask();
        scanTask.setBuckets(consumerBucket.getBuckets());

        // 扫描定时任务数据
        ActorRef scanJobActorRef = cacheActorRef(DEFAULT_JOB_KEY, SystemTaskType.JOB);
        scanJobActorRef.tell(scanTask, scanJobActorRef);

        // 扫描DAG工作流任务数据
        ActorRef scanWorkflowActorRef = cacheActorRef(DEFAULT_WORKFLOW_KEY, SystemTaskType.WORKFLOW);
        scanWorkflowActorRef.tell(scanTask, scanWorkflowActorRef);
    }

    /**
     * 缓存Actor对象
     */
    private ActorRef cacheActorRef(String groupName, SystemTaskType typeEnum) {
        ActorRef scanActorRef = CacheGroupScanActor.get(groupName, typeEnum);
        if (Objects.isNull(scanActorRef)) {
            scanActorRef = ACTORREF_MAP.get(typeEnum).get();
            // 缓存扫描器actor
            CacheGroupScanActor.put(groupName, typeEnum, scanActorRef);
        }
        return scanActorRef;
    }
}
