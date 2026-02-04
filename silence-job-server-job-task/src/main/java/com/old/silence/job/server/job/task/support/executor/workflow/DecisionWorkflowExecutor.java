package com.old.silence.job.server.job.task.support.executor.workflow;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.common.enums.JobTaskStatus;
import com.old.silence.job.common.enums.WorkflowNodeType;
import com.old.silence.job.common.expression.ExpressionEngine;
import com.old.silence.job.common.expression.ExpressionFactory;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.dto.JobLogMetaDTO;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.domain.model.WorkflowTaskBatch;
import com.old.silence.job.server.dto.DecisionConfig;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowTaskBatchDao;
import com.old.silence.job.server.job.task.dto.WorkflowTaskFailAlarmEventDTO;
import com.old.silence.job.server.job.task.support.alarm.event.WorkflowTaskFailAlarmEvent;
import com.old.silence.job.server.job.task.support.expression.ExpressionInvocationHandler;
import com.old.silence.job.server.job.task.support.generator.batch.JobTaskBatchGenerator;
import com.old.silence.job.server.job.task.support.handler.DistributedLockHandler;
import com.old.silence.job.server.job.task.support.handler.WorkflowBatchHandler;

import java.util.Objects;
import java.util.Optional;

import static com.old.silence.job.common.enums.JobOperationReason.WORKFLOW_SUCCESSOR_SKIP_EXECUTION;


@Component

public class DecisionWorkflowExecutor extends AbstractWorkflowExecutor {

    private static final Logger log = LoggerFactory.getLogger(DecisionWorkflowExecutor.class);
    private final WorkflowTaskBatchDao workflowTaskBatchDao;

    protected DecisionWorkflowExecutor(DistributedLockHandler distributedLockHandler,
                                       JobTaskBatchDao jobTaskBatchDao, JobTaskBatchGenerator jobTaskBatchGenerator, WorkflowBatchHandler workflowBatchHandler, JobTaskDao jobTaskDao, TransactionTemplate transactionTemplate, WorkflowTaskBatchDao workflowTaskBatchDao) {
        super(distributedLockHandler, jobTaskBatchDao, jobTaskBatchGenerator, workflowBatchHandler, jobTaskDao, transactionTemplate);
        this.workflowTaskBatchDao = workflowTaskBatchDao;
    }


    @Override
    public WorkflowNodeType getWorkflowNodeType() {
        return WorkflowNodeType.DECISION;
    }

    @Override
    protected void beforeExecute(WorkflowExecutorContext context) {

    }

    @Override
    public void doExecute(WorkflowExecutorContext context) {
        var taskBatchStatus = JobTaskBatchStatus.SUCCESS;
        var operationReason = JobOperationReason.NONE;
        var jobTaskStatus = JobTaskStatus.SUCCESS;
        String message = StrUtil.EMPTY;
        String wfContext = "";

        Boolean result = (Boolean) Optional.ofNullable(context.getEvaluationResult()).orElse(Boolean.FALSE);

        if (result || (WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(context.getParentOperationReason()))) {
            // 多个条件节点直接是或的关系，只要一个成功其他节点就取消且是无需处理状态
            taskBatchStatus = JobTaskBatchStatus.CANCEL;
            jobTaskStatus = JobTaskStatus.CANCEL;
            operationReason = JobOperationReason.WORKFLOW_NODE_NO_REQUIRED;
        } else {
            DecisionConfig decisionConfig = JSON.parseObject(context.getNodeInfo(), DecisionConfig.class);
            if (!decisionConfig.getDefaultDecision()) {

                try {
                    // 这里重新加载一次最新的上下文
                    WorkflowTaskBatch workflowTaskBatch = workflowTaskBatchDao.selectOne(new LambdaQueryWrapper<WorkflowTaskBatch>()
                            .select(WorkflowTaskBatch::getWfContext)
                            .eq(WorkflowTaskBatch::getId, context.getWorkflowTaskBatchId())
                    );

                    if (Objects.isNull(workflowTaskBatch)) {
                        operationReason = JobOperationReason.WORKFLOW_DECISION_FAILED;
                    } else {
                        wfContext = workflowTaskBatch.getWfContext();
                        ExpressionEngine realExpressionEngine = ExpressionFactory.getExpressionEngineByType(decisionConfig.getExpressionType());
                        Assert.notNull(realExpressionEngine, () -> new SilenceJobServerException("表达式引擎不存在"));
                        ExpressionInvocationHandler invocationHandler = new ExpressionInvocationHandler(realExpressionEngine);
                        ExpressionEngine expressionEngine = ExpressionFactory.getExpressionEngine(invocationHandler);
                        result = (Boolean) Optional.ofNullable(expressionEngine.eval(decisionConfig.getNodeExpression(), wfContext)).orElse(Boolean.FALSE);
                        if (!result) {
                            operationReason = JobOperationReason.WORKFLOW_DECISION_FAILED;
                        }
                    }

                } catch (Exception e) {
                    log.error("执行条件表达式解析异常. 表达式:[{}]，参数: [{}]", decisionConfig.getNodeExpression(), wfContext, e);
                    taskBatchStatus = JobTaskBatchStatus.FAIL;
                    operationReason = JobOperationReason.WORKFLOW_CONDITION_NODE_EXECUTION_ERROR;
                    jobTaskStatus = JobTaskStatus.FAIL;
                    message = e.getMessage();

                    var workflowTaskFailAlarmEventDTO = new WorkflowTaskFailAlarmEventDTO();
                    workflowTaskFailAlarmEventDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
                    workflowTaskFailAlarmEventDTO.setNotifyScene(JobNotifyScene.WORKFLOW_TASK_ERROR);
                    workflowTaskFailAlarmEventDTO.setReason(message);
                    SilenceSpringContext.getContext().publishEvent(new WorkflowTaskFailAlarmEvent(workflowTaskFailAlarmEventDTO));
                }
            } else {
                result = Boolean.TRUE;
            }
        }

        // 回传执行结果
        context.setEvaluationResult(result);
        context.setTaskBatchStatus(taskBatchStatus);
        context.setOperationReason(operationReason);
        context.setJobTaskStatus(jobTaskStatus);
        context.setLogMessage(message);
        context.setWfContext(wfContext);

    }

    @Override
    protected boolean doPreValidate(WorkflowExecutorContext context) {
        return true;
    }

    @Override
    protected void afterExecute(WorkflowExecutorContext context) {

        JobTaskBatch jobTaskBatch = generateJobTaskBatch(context);

        JobTask jobTask = generateJobTask(context, jobTaskBatch);

        JobLogMetaDTO jobLogMetaDTO = new JobLogMetaDTO();
        jobLogMetaDTO.setNamespaceId(context.getNamespaceId());
        jobLogMetaDTO.setGroupName(context.getGroupName());
        jobLogMetaDTO.setTaskBatchId(jobTaskBatch.getId());
        jobLogMetaDTO.setJobId(SystemConstants.DECISION_JOB_ID);
        jobLogMetaDTO.setTaskId(jobTask.getId());
        if (jobTaskBatch.getTaskBatchStatus().equals(JobTaskBatchStatus.SUCCESS)
                || JobOperationReason.WORKFLOW_NODE_NO_REQUIRED.equals(context.getOperationReason())) {

            SilenceJobLog.REMOTE.info("节点Id:[{}] 决策完成. 上下文:[{}] 决策结果:[{}] <|>{}<|>",
                    context.getWorkflowNodeId(), context.getWfContext(), context.getEvaluationResult(), jobLogMetaDTO);
        } else {
            SilenceJobLog.REMOTE.error("节点Id:[{}] 决策失败. 上下文:[{}] 失败原因:[{}] <|>{}<|>",
                    context.getWorkflowNodeId(), context.getWfContext(), context.getLogMessage(), jobLogMetaDTO);

        }
    }
}
