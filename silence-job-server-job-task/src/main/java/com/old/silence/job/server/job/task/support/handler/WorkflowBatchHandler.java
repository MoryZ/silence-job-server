package com.old.silence.job.server.job.task.support.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import org.apache.pekko.actor.ActorRef;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.graph.MutableGraph;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.common.enums.JobTaskExecutorScene;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.domain.model.WorkflowTaskBatch;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowTaskBatchDao;
import com.old.silence.job.server.job.task.dto.JobTaskPrepareDTO;
import com.old.silence.job.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.old.silence.job.server.job.task.dto.WorkflowTaskFailAlarmEventDTO;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.job.task.support.JobTaskStopHandler;
import com.old.silence.job.server.job.task.support.alarm.event.WorkflowTaskFailAlarmEvent;
import com.old.silence.job.server.job.task.support.cache.MutableGraphCache;
import com.old.silence.job.server.job.task.support.stop.JobTaskStopFactory;
import com.old.silence.job.server.job.task.support.stop.TaskStopJobContext;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.io.IOException;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.old.silence.job.common.enums.JobOperationReason.WORKFLOW_SUCCESSOR_SKIP_EXECUTION;
import static com.old.silence.job.common.enums.JobTaskBatchStatus.NOT_COMPLETE;


@Component
public class WorkflowBatchHandler {
    private static final String KEY = "update_wf_context_{0}";

    private final DistributedLockHandler distributedLockHandler;
    private final WorkflowTaskBatchDao workflowTaskBatchDao;
    private final JobDao jobDao;
    private final JobTaskBatchDao jobTaskBatchDao;
    private final JobTaskDao jobTaskDao;

    public WorkflowBatchHandler(DistributedLockHandler distributedLockHandler, WorkflowTaskBatchDao workflowTaskBatchDao,
                                JobDao jobDao, JobTaskBatchDao jobTaskBatchDao, JobTaskDao jobTaskDao) {
        this.distributedLockHandler = distributedLockHandler;
        this.workflowTaskBatchDao = workflowTaskBatchDao;
        this.jobDao = jobDao;
        this.jobTaskBatchDao = jobTaskBatchDao;
        this.jobTaskDao = jobTaskDao;
    }

    private static boolean checkLeafCompleted(MutableGraph<BigInteger> graph, Map<BigInteger,
            List<JobTaskBatch>> currentWorkflowNodeMap, Set<BigInteger> parentIds) {

        // 判定子节点是否需要处理
        boolean isNeedProcess = true;
        for (BigInteger nodeId : parentIds) {
            List<JobTaskBatch> jobTaskBatchList = currentWorkflowNodeMap.get(nodeId);
            if (CollectionUtils.isEmpty(jobTaskBatchList)) {
                // 递归查询有执行过的任务批次
                isNeedProcess = isNeedProcess || checkLeafCompleted(graph, currentWorkflowNodeMap, graph.predecessors(nodeId));
                continue;
            }

            for (JobTaskBatch jobTaskBatch : jobTaskBatchList) {
                // 只要是无需处理的说明后面的子节点都不需要处理了，isNeedProcess为false
                if (WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(jobTaskBatch.getOperationReason())) {
                    isNeedProcess = false;
                    continue;
                }

                isNeedProcess = true;
            }

        }

        return isNeedProcess;
    }

    public static void mergeMaps(Map<String, Object> mainMap, Map<String, Object> waitMergeMap) {
        for (Map.Entry<String, Object> entry : waitMergeMap.entrySet()) {
            if (Objects.isNull(entry.getKey()) || Objects.isNull(entry.getValue())) {
                SilenceJobLog.LOCAL.warn("上下文的key和value不支持NULl");
                continue;
            }
            mainMap.merge(entry.getKey(), entry, (v1, v2) -> v2);
        }
    }

    public boolean complete(BigInteger workflowTaskBatchId) {
        return complete(workflowTaskBatchId, null);
    }

    public boolean complete(BigInteger workflowTaskBatchId, WorkflowTaskBatch workflowTaskBatch) {
        workflowTaskBatch = Optional.ofNullable(workflowTaskBatch)
                .orElseGet(() -> workflowTaskBatchDao.selectById(workflowTaskBatchId));
        Assert.notNull(workflowTaskBatch, () -> new SilenceJobServerException("任务不存在"));

        String flowInfo = workflowTaskBatch.getFlowInfo();
        MutableGraph<BigInteger> graph = MutableGraphCache.getOrDefault(workflowTaskBatchId, flowInfo);

        // 说明没有后继节点了, 此时需要判断整个DAG是否全部执行完成
        List<JobTaskBatch> jobTaskBatches = jobTaskBatchDao.selectList(new LambdaQueryWrapper<JobTaskBatch>()
                .eq(JobTaskBatch::getWorkflowTaskBatchId, workflowTaskBatch.getId())
                .in(JobTaskBatch::getWorkflowNodeId, graph.nodes())
        );

        if (CollectionUtils.isEmpty(jobTaskBatches)) {
            return false;
        }

        if (jobTaskBatches.stream().anyMatch(
                jobTaskBatch -> JobTaskBatchStatus.NOT_COMPLETE.contains(jobTaskBatch.getTaskBatchStatus()))) {
            return false;
        }

        Map<BigInteger, List<JobTaskBatch>> currentWorkflowNodeMap = StreamUtils.groupByKey(jobTaskBatches,
                JobTaskBatch::getWorkflowNodeId);

        // 判定最后的工作流批次状态
        JobTaskBatchStatus taskStatus = JobTaskBatchStatus.SUCCESS;
        JobOperationReason operationReason = JobOperationReason.NONE;

        // 判定所有的叶子节点是否完成
        List<BigInteger> leaves = MutableGraphCache.getLeaves(workflowTaskBatchId, flowInfo);
        for (BigInteger leaf : leaves) {
            List<JobTaskBatch> jobTaskBatchList = currentWorkflowNodeMap.getOrDefault(leaf, Lists.newArrayList());
            if (CollectionUtils.isEmpty(jobTaskBatchList)) {
                boolean isNeedProcess = checkLeafCompleted(graph, currentWorkflowNodeMap, graph.predecessors(leaf));
                // 说明当前叶子节点需要处理，但是未处理返回false
                if (isNeedProcess) {
                    return false;
                }
            }

            boolean isMatchSuccess = jobTaskBatchList.stream()
                    .anyMatch(jobTaskBatch -> JobTaskBatchStatus.SUCCESS.equals(jobTaskBatch.getTaskBatchStatus()));
            if (!isMatchSuccess) {
                // 判定叶子节点的状态
                for (JobTaskBatch jobTaskBatch : jobTaskBatchList) {
                    if (jobTaskBatch.getTaskBatchStatus().equals(JobTaskBatchStatus.SUCCESS)) {

                        break;
                    } else if (JobTaskBatchStatus.NOT_SUCCESS.contains(jobTaskBatch.getTaskBatchStatus())) {
                        // 只要叶子节点不是无需处理的都是失败
                        if (!JobOperationReason.WORKFLOW_NODE_NO_REQUIRED.equals(jobTaskBatch.getOperationReason())
                                && !JobOperationReason.WORKFLOW_NODE_CLOSED_SKIP_EXECUTION.equals(jobTaskBatch.getOperationReason())) {

                            taskStatus = JobTaskBatchStatus.FAIL;

                            var workflowTaskFailAlarmEventDTO = new WorkflowTaskFailAlarmEventDTO();
                            workflowTaskFailAlarmEventDTO.setWorkflowTaskBatchId(workflowTaskBatchId);
                            workflowTaskFailAlarmEventDTO.setNotifyScene(JobNotifyScene.WORKFLOW_TASK_ERROR);
                            workflowTaskFailAlarmEventDTO.setReason("任务执行失败 jobTaskBatchId:" + jobTaskBatch.getId());
                            SilenceSpringContext.getContext().publishEvent(new WorkflowTaskFailAlarmEvent(
                                    workflowTaskFailAlarmEventDTO));

                        }
                    }
                }
            }
        }

        handlerTaskBatch(workflowTaskBatchId, taskStatus, operationReason);

        return true;

    }

    private void handlerTaskBatch(BigInteger workflowTaskBatchId, JobTaskBatchStatus taskStatus, JobOperationReason operationReason) {

        WorkflowTaskBatch jobTaskBatch = new WorkflowTaskBatch();
        jobTaskBatch.setId(workflowTaskBatchId);
        jobTaskBatch.setTaskBatchStatus(taskStatus);
        jobTaskBatch.setOperationReason(operationReason);
        workflowTaskBatchDao.updateById(jobTaskBatch);
    }

    public void stop(BigInteger workflowTaskBatchId, JobOperationReason operationReason) {
        if (Objects.isNull(operationReason)
                || operationReason.equals(JobOperationReason.NONE)) {

            operationReason = JobOperationReason.JOB_OVERLAY;
        }

        WorkflowTaskBatch workflowTaskBatch = new WorkflowTaskBatch();
        workflowTaskBatch.setTaskBatchStatus(JobTaskBatchStatus.STOP);
        workflowTaskBatch.setOperationReason(operationReason);
        workflowTaskBatch.setId(workflowTaskBatchId);
        // 先停止执行中的批次
        Assert.isTrue(1 == workflowTaskBatchDao.updateById(workflowTaskBatch),
                () -> new SilenceJobServerException("停止工作流批次失败. id:[{}]",
                        workflowTaskBatchId));

        var workflowTaskFailAlarmEventDTO = new WorkflowTaskFailAlarmEventDTO();
        workflowTaskFailAlarmEventDTO.setWorkflowTaskBatchId(workflowTaskBatchId);
        workflowTaskFailAlarmEventDTO.setNotifyScene(JobNotifyScene.WORKFLOW_TASK_ERROR);
        workflowTaskFailAlarmEventDTO.setReason("停止工作流批次失败");
        SilenceSpringContext.getContext().publishEvent(new WorkflowTaskFailAlarmEvent(workflowTaskFailAlarmEventDTO));

        // 关闭已经触发的任务
        List<JobTaskBatch> jobTaskBatches = jobTaskBatchDao.selectList(new LambdaQueryWrapper<JobTaskBatch>()
                .in(JobTaskBatch::getTaskBatchStatus, NOT_COMPLETE)
                .eq(JobTaskBatch::getWorkflowTaskBatchId, workflowTaskBatchId));

        if (CollectionUtils.isEmpty(jobTaskBatches)) {
            return;
        }

        List<Job> jobs = jobDao.selectBatchIds(StreamUtils.toSet(jobTaskBatches, JobTaskBatch::getJobId));

        Map<BigInteger, Job> jobMap = StreamUtils.toIdentityMap(jobs, Job::getId);
        for (JobTaskBatch jobTaskBatch : jobTaskBatches) {

            Job job = jobMap.get(jobTaskBatch.getJobId());
            if (Objects.nonNull(job)) {
                // 停止任务
                JobTaskStopHandler instanceInterrupt = JobTaskStopFactory.getJobTaskStop(job.getTaskType());
                TaskStopJobContext stopJobContext = JobTaskConverter.INSTANCE.toStopJobContext(job);
                stopJobContext.setTaskBatchId(jobTaskBatch.getId());
                stopJobContext.setJobOperationReason(JobOperationReason.JOB_TASK_INTERRUPTED);
                stopJobContext.setNeedUpdateTaskStatus(Boolean.TRUE);
                stopJobContext.setForceStop(Boolean.TRUE);
                instanceInterrupt.stop(stopJobContext);
            }

        }
    }

    /**
     * 重新触发未执行成功的工作流节点
     *
     * @param workflowTaskBatchId 工作流批次
     * @param workflowTaskBatch   工作流批次信息(若为null, 则会通过workflowTaskBatchId查询)
     * @throws IOException
     */
    public void recoveryWorkflowExecutor(BigInteger workflowTaskBatchId, WorkflowTaskBatch workflowTaskBatch) throws IOException {
        workflowTaskBatch = Optional.ofNullable(workflowTaskBatch)
                .orElseGet(() -> workflowTaskBatchDao.selectById(workflowTaskBatchId));
        Assert.notNull(workflowTaskBatch, () -> new SilenceJobServerException("任务不存在"));
        String flowInfo = workflowTaskBatch.getFlowInfo();
        MutableGraph<BigInteger> graph = MutableGraphCache.getOrDefault(workflowTaskBatchId, flowInfo);
        Set<BigInteger> successors = graph.successors(SystemConstants.ROOT);
        if (CollectionUtils.isEmpty(successors)) {
            return;
        }

        // 说明没有后继节点了, 此时需要判断整个DAG是否全部执行完成
        List<JobTaskBatch> jobTaskBatches = jobTaskBatchDao.selectList(new LambdaQueryWrapper<JobTaskBatch>()
                .eq(JobTaskBatch::getWorkflowTaskBatchId, workflowTaskBatchId)
                .in(JobTaskBatch::getWorkflowNodeId, graph.nodes()).orderByDesc(JobTaskBatch::getId)
        );

        Map<BigInteger, JobTaskBatch> jobTaskBatchMap = StreamUtils.toIdentityMap(jobTaskBatches, JobTaskBatch::getWorkflowNodeId);

        recoveryWorkflowExecutor(SystemConstants.ROOT, workflowTaskBatchId, graph, jobTaskBatchMap);
    }

    private void recoveryWorkflowExecutor(BigInteger parentId, BigInteger workflowTaskBatchId, MutableGraph<BigInteger> graph, Map<BigInteger, JobTaskBatch> jobTaskBatchMap) {

        // 判定条件节点是否已经执行完成
        JobTaskBatch parentJobTaskBatch = jobTaskBatchMap.get(parentId);
        if (Objects.nonNull(parentJobTaskBatch) &&
                WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(parentJobTaskBatch.getOperationReason())) {
            return;
        }

        Set<BigInteger> successors = graph.successors(parentId);
        if (CollectionUtils.isEmpty(successors)) {
            return;
        }

        for (BigInteger successor : successors) {
            JobTaskBatch jobTaskBatch = jobTaskBatchMap.get(successor);
            if (Objects.isNull(jobTaskBatch)) {
                // 重新尝试执行, 重新生成任务批次
                WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
                taskExecuteDTO.setWorkflowTaskBatchId(workflowTaskBatchId);
                taskExecuteDTO.setTaskExecutorScene(JobTaskExecutorScene.AUTO_WORKFLOW);
                taskExecuteDTO.setParentId(parentId);
                if (Objects.nonNull(parentJobTaskBatch)) {
                    taskExecuteDTO.setTaskBatchId(parentJobTaskBatch.getId());
                }
                openNextNode(taskExecuteDTO);
                break;
            }

            if (NOT_COMPLETE.contains(jobTaskBatch.getTaskBatchStatus())) {
                // 生成任务批次
                Job job = jobDao.selectById(jobTaskBatch.getJobId());
                JobTaskPrepareDTO jobTaskPrepare = JobTaskConverter.INSTANCE.toJobTaskPrepare(job);
                jobTaskPrepare.setTaskExecutorScene(JobTaskExecutorScene.AUTO_WORKFLOW);
                jobTaskPrepare.setNextTriggerAt(DateUtils.toNowMilli() + DateUtils.toNowMilli() % 1000);
                jobTaskPrepare.setWorkflowTaskBatchId(workflowTaskBatchId);
                jobTaskPrepare.setWorkflowNodeId(successor);
                jobTaskPrepare.setParentWorkflowNodeId(parentId);
                // 执行预处理阶段
                ActorRef actorRef = ActorGenerator.jobTaskPrepareActor();
                actorRef.tell(jobTaskPrepare, actorRef);
                break;
            }

            // 已经是终态的需要递归遍历后继节点是否正常执行
            recoveryWorkflowExecutor(successor, workflowTaskBatchId, graph, jobTaskBatchMap);
        }
    }

    public void openNextNode(WorkflowNodeTaskExecuteDTO taskExecuteDTO) {
        if (Objects.isNull(taskExecuteDTO.getParentId()) || Objects.isNull(taskExecuteDTO.getWorkflowTaskBatchId()) || BigInteger.ZERO.compareTo(taskExecuteDTO.getWorkflowTaskBatchId()) == 0) {
            return;
        }

        // 若是工作流则开启下一个任务
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    tellWorkflowTaskExecutor(taskExecuteDTO);
                }
            });
        } else {
            tellWorkflowTaskExecutor(taskExecuteDTO);
        }
    }

    private void tellWorkflowTaskExecutor(WorkflowNodeTaskExecuteDTO taskExecuteDTO) {
        try {
            ActorRef actorRef = ActorGenerator.workflowTaskExecutorActor();
            actorRef.tell(taskExecuteDTO, actorRef);
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("任务调度执行失败", e);
        }
    }

    /**
     * 合并工作流上下文若合并失败先自旋3次1.5s, 若失败了升级到悲观锁
     *
     * @param workflowTaskBatch 工作流批次
     * @param taskBatchIds      批次列表
     */
    public void mergeWorkflowContextAndRetry(WorkflowTaskBatch workflowTaskBatch, Set<BigInteger> taskBatchIds) {
        if (CollectionUtils.isEmpty(taskBatchIds)) {
            return;
        }

        // 自旋更新
        Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                .retryIfResult(result -> result.equals(Boolean.FALSE))
                .retryIfException(ex -> true)
                .withWaitStrategy(WaitStrategies.randomWait(800, TimeUnit.MILLISECONDS, 2000, TimeUnit.MILLISECONDS))
                // 重试3秒
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        Object result = null;
                        if (attempt.hasResult()) {
                            try {
                                result = attempt.get();
                            } catch (ExecutionException ignored) {
                            }
                        }

                        SilenceJobLog.LOCAL.info("第【{}】次尝试更新上下文.taskBatchIds:[{}]  result:[{}] treadName:[{}] ",
                                attempt.getAttemptNumber(), taskBatchIds, result, Thread.currentThread().getName());
                    }
                }).build();

        try {
            retryer.call(() -> mergeAllWorkflowContext(workflowTaskBatch, taskBatchIds));
        } catch (Exception e) {
            SilenceJobLog.LOCAL.warn("update workflow global context error. workflowTaskBatchId:[{}] taskBatchIds:[{}]",
                    workflowTaskBatch.getId(), taskBatchIds, e);
            if (e.getClass().isAssignableFrom(RetryException.class)) {
                // 如果自旋失败，就使用悲观锁
                distributedLockHandler.lockWithDisposableAndRetry(() -> {
                    mergeAllWorkflowContext(workflowTaskBatch, taskBatchIds);
                }, MessageFormat.format(KEY, workflowTaskBatch.getId()), Duration.ofSeconds(1), Duration.ofSeconds(1), 3);
            }
        }
    }

    public boolean mergeAllWorkflowContext(WorkflowTaskBatch workflowTaskBatch, Set<BigInteger> taskBatchIds) {
        if (CollectionUtils.isEmpty(taskBatchIds)) {
            return true;
        }

        List<JobTask> jobTasks = jobTaskDao.selectList(new LambdaQueryWrapper<JobTask>()
                .select(JobTask::getWfContext, JobTask::getId)
                .in(JobTask::getTaskBatchId, taskBatchIds)
        );
        if (CollectionUtils.isEmpty(jobTasks)) {
            return true;
        }
        var type = new TypeReference<Map<String, Object>>() {
        }.getType();
        Set<Map<String, Object>> maps = jobTasks.stream().map(r -> {
            try {
                if (StrUtil.isNotBlank(r.getWfContext())) {

                    return JSON.parseObject(r.getWfContext(), type);
                }
            } catch (Exception e) {
                SilenceJobLog.LOCAL.warn("taskId:[{}] result value is not a JSON object. result:[{}]", r.getId(), r.getResultMessage());
            }
            return new HashMap<String, Object>();
        }).collect(Collectors.toSet());

        Map<String, Object> mergeMap;
        if (StrUtil.isBlank(workflowTaskBatch.getWfContext())) {
            mergeMap = Maps.newHashMap();
        } else {
            mergeMap = JSON.parseObject(workflowTaskBatch.getWfContext(), type);
        }

        for (Map<String, Object> map : maps) {
            mergeMaps(mergeMap, map);
        }

        WorkflowTaskBatch waitUpdateWorkflowTaskBatch = new WorkflowTaskBatch();
        waitUpdateWorkflowTaskBatch.setId(workflowTaskBatch.getId());
        waitUpdateWorkflowTaskBatch.setWfContext(JSON.toJSONString(mergeMap));
        waitUpdateWorkflowTaskBatch.setVersion(1);
        return 1 == workflowTaskBatchDao.update(waitUpdateWorkflowTaskBatch, new LambdaQueryWrapper<WorkflowTaskBatch>()
                .eq(WorkflowTaskBatch::getId, workflowTaskBatch.getId())
                .eq(WorkflowTaskBatch::getVersion, workflowTaskBatch.getVersion())
        );
    }

    /**
     * 合并客户端上报的上下问题信息
     *
     * @param workflowTaskBatchId 工作流批次
     * @param waitMergeContext    待合并的上下文
     */
    public boolean mergeWorkflowContext(BigInteger workflowTaskBatchId, Map<String, Object> waitMergeContext) {
        if (CollectionUtils.isEmpty(waitMergeContext) || Objects.isNull(workflowTaskBatchId)) {
            return true;
        }

        WorkflowTaskBatch workflowTaskBatch = workflowTaskBatchDao.selectOne(
                new LambdaQueryWrapper<WorkflowTaskBatch>()
                        .select(WorkflowTaskBatch::getWfContext, WorkflowTaskBatch::getVersion)
                        .eq(WorkflowTaskBatch::getId, workflowTaskBatchId)
        );

        if (Objects.isNull(workflowTaskBatch)) {
            return true;
        }

        String wfContext = workflowTaskBatch.getWfContext();
        if (StrUtil.isNotBlank(wfContext)) {
            var typeReference = new TypeReference<>() {
            }.getType();
            mergeMaps(waitMergeContext, JSON.parseObject(wfContext, typeReference));
        }

        int version = workflowTaskBatch.getVersion();
        workflowTaskBatch.setWfContext(JSON.toJSONString(waitMergeContext));
        workflowTaskBatch.setVersion(null);
        return 1 == workflowTaskBatchDao.update(workflowTaskBatch, new LambdaQueryWrapper<WorkflowTaskBatch>()
                .eq(WorkflowTaskBatch::getId, workflowTaskBatchId)
                .eq(WorkflowTaskBatch::getVersion, version)
        );
    }
}
