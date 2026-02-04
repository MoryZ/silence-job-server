package com.old.silence.job.server.job.task.support.executor.workflow;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.JobArgsType;
import com.old.silence.job.common.enums.WorkflowNodeType;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.old.silence.job.server.job.task.support.WorkflowExecutor;
import com.old.silence.job.server.job.task.support.WorkflowTaskConverter;
import com.old.silence.job.server.job.task.support.generator.batch.JobTaskBatchGenerator;
import com.old.silence.job.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;
import com.old.silence.job.server.job.task.support.handler.DistributedLockHandler;
import com.old.silence.job.server.job.task.support.handler.WorkflowBatchHandler;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;

import static com.old.silence.job.common.enums.JobOperationReason.WORKFLOW_SUCCESSOR_SKIP_EXECUTION;


public abstract class AbstractWorkflowExecutor implements WorkflowExecutor, InitializingBean {


    private static final String KEY = "workflow_execute_{0}_{1}";
    private static final Logger log = LoggerFactory.getLogger(AbstractWorkflowExecutor.class);
    protected final WorkflowBatchHandler workflowBatchHandler;
    private final DistributedLockHandler distributedLockHandler;
    private final JobTaskBatchDao jobTaskBatchDao;
    private final JobTaskBatchGenerator jobTaskBatchGenerator;
    private final JobTaskDao jobTaskDao;
    private final TransactionTemplate transactionTemplate;

    protected AbstractWorkflowExecutor(DistributedLockHandler distributedLockHandler, JobTaskBatchDao jobTaskBatchDao, JobTaskBatchGenerator jobTaskBatchGenerator, WorkflowBatchHandler workflowBatchHandler, JobTaskDao jobTaskDao, TransactionTemplate transactionTemplate) {
        this.distributedLockHandler = distributedLockHandler;
        this.jobTaskBatchDao = jobTaskBatchDao;
        this.jobTaskBatchGenerator = jobTaskBatchGenerator;
        this.workflowBatchHandler = workflowBatchHandler;
        this.jobTaskDao = jobTaskDao;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void execute(WorkflowExecutorContext context) {

        // 若多个兄弟节点的情况下，同时处理完成则每个节点都有可能来执行后继节点，
        // 因此这里这里添加分布式锁
        distributedLockHandler.lockWithDisposableAndRetry(
                () -> {
                    long total = 0;
                    // 条件节点存在并发问题，需要特殊处理
                    if (WorkflowNodeType.DECISION.equals(context.getNodeType())) {

                        List<JobTaskBatch> jobTaskBatches = jobTaskBatchDao.selectList(new LambdaQueryWrapper<JobTaskBatch>()
                                .select(JobTaskBatch::getOperationReason)
                                .eq(JobTaskBatch::getWorkflowTaskBatchId, context.getWorkflowTaskBatchId())
                                .eq(JobTaskBatch::getWorkflowNodeId, context.getWorkflowNodeId())
                        );

                        if (CollectionUtils.isNotEmpty(jobTaskBatches)) {
                            total = jobTaskBatches.size();
                            // ToDo
                            JobTaskBatch jobTaskBatch = jobTaskBatches.get(0);
                            if (WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(jobTaskBatch.getOperationReason())) {
                                context.setEvaluationResult(Boolean.FALSE);
                            } else {
                                context.setEvaluationResult(Boolean.TRUE);
                            }
                        }

                    } else {
                        total = jobTaskBatchDao.selectCount(new LambdaQueryWrapper<JobTaskBatch>()
                                .eq(JobTaskBatch::getWorkflowTaskBatchId, context.getWorkflowTaskBatchId())
                                .eq(JobTaskBatch::getWorkflowNodeId, context.getWorkflowNodeId())
                        );
                    }

                    if (total > 0) {
                        log.warn("任务节点[{}]已被执行，请勿重复执行", context.getWorkflowNodeId());
                        return;
                    }

                    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                        @Override
                        protected void doInTransactionWithoutResult(final TransactionStatus status) {

                            if (!preValidate(context)) {
                                return;
                            }
                            beforeExecute(context);

                            doExecute(context);

                            afterExecute(context);
                        }
                    });
                }, MessageFormat.format(KEY, context.getWorkflowTaskBatchId(), context.getWorkflowNodeId()),
                Duration.ofSeconds(10), Duration.ofSeconds(3), 16);

    }

    protected boolean preValidate(WorkflowExecutorContext context) {
        return doPreValidate(context);
    }

    protected abstract boolean doPreValidate(WorkflowExecutorContext context);

    protected abstract void afterExecute(WorkflowExecutorContext context);

    protected abstract void beforeExecute(WorkflowExecutorContext context);

    protected abstract void doExecute(WorkflowExecutorContext context);

    protected JobTaskBatch generateJobTaskBatch(WorkflowExecutorContext context) {
        JobTaskBatchGeneratorContext generatorContext = WorkflowTaskConverter.INSTANCE.toJobTaskBatchGeneratorContext(context);
        return jobTaskBatchGenerator.generateJobTaskBatch(generatorContext);
    }

    protected void workflowTaskExecutor(WorkflowExecutorContext context) {
        WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
        taskExecuteDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
        taskExecuteDTO.setTaskExecutorScene(context.getTaskExecutorScene());
        taskExecuteDTO.setParentId(context.getWorkflowNodeId());
        taskExecuteDTO.setTaskBatchId(context.getTaskBatchId());
        workflowBatchHandler.openNextNode(taskExecuteDTO);
    }

    protected JobTask generateJobTask(WorkflowExecutorContext context, JobTaskBatch jobTaskBatch) {
        // 生成执行任务实例
        JobTask jobTask = new JobTask();
        jobTask.setGroupName(context.getGroupName());
        jobTask.setNamespaceId(context.getNamespaceId());
        jobTask.setJobId(context.getJobId());
        jobTask.setClientInfo(StrUtil.EMPTY);
        jobTask.setTaskBatchId(jobTaskBatch.getId());
        jobTask.setArgsType(JobArgsType.TEXT);
        jobTask.setTaskStatus(context.getJobTaskStatus());
        jobTask.setResultMessage(String.valueOf(context.getEvaluationResult()));
        Assert.isTrue(1 == jobTaskDao.insert(jobTask), () -> new SilenceJobServerException("新增任务实例失败"));
        return jobTask;
    }

    @Override
    public void afterPropertiesSet() {
        WorkflowExecutorFactory.registerJobExecutor(getWorkflowNodeType(), this);
    }
}
