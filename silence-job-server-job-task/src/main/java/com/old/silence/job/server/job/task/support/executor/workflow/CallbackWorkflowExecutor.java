package com.old.silence.job.server.job.task.support.executor.workflow;

import cn.hutool.core.util.StrUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson2.JSON;
import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.common.enums.JobTaskStatus;
import com.old.silence.job.common.enums.WorkflowNodeType;
import com.old.silence.job.common.server.dto.CallbackParamsDTO;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.dto.JobLogMetaDTO;
import com.old.silence.job.server.common.rpc.okhttp.RequestInterceptor;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.dto.CallbackConfig;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.job.task.dto.WorkflowTaskFailAlarmEventDTO;
import com.old.silence.job.server.job.task.support.alarm.event.WorkflowTaskFailAlarmEvent;
import com.old.silence.job.server.job.task.support.generator.batch.JobTaskBatchGenerator;
import com.old.silence.job.server.job.task.support.handler.DistributedLockHandler;
import com.old.silence.job.server.job.task.support.handler.WorkflowBatchHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.old.silence.job.common.enums.JobOperationReason.WORKFLOW_SUCCESSOR_SKIP_EXECUTION;


@Component
public class CallbackWorkflowExecutor extends AbstractWorkflowExecutor {

    private static final String CALLBACK_TIMEOUT = "10";
    private final RestTemplate restTemplate;

    protected CallbackWorkflowExecutor(DistributedLockHandler distributedLockHandler, JobTaskBatchDao jobTaskBatchDao,
                                       JobTaskBatchGenerator jobTaskBatchGenerator, WorkflowBatchHandler workflowBatchHandler, JobTaskDao jobTaskDao, TransactionTemplate transactionTemplate, RestTemplate restTemplate) {
        super(distributedLockHandler, jobTaskBatchDao, jobTaskBatchGenerator, workflowBatchHandler, jobTaskDao, transactionTemplate);
        this.restTemplate = restTemplate;
    }

    private static Retryer<ResponseEntity<String>> buildRetryer(CallbackConfig decisionConfig) {
        Retryer<ResponseEntity<String>> retryer = RetryerBuilder.<ResponseEntity<String>>newBuilder()
                .retryIfException(throwable -> true)
                .withWaitStrategy(WaitStrategies.fixedWait(150, TimeUnit.MILLISECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(10))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(final Attempt<V> attempt) {
                        if (attempt.hasException()) {
                            SilenceJobLog.LOCAL.error("回调接口第 【{}】 重试. 回调配置信息: [{}]",
                                    attempt.getAttemptNumber(), JSON.toJSONString(decisionConfig));
                        }
                    }
                }).build();
        return retryer;
    }

    @Override
    public WorkflowNodeType getWorkflowNodeType() {
        return WorkflowNodeType.CALLBACK;
    }

    @Override
    protected void beforeExecute(WorkflowExecutorContext context) {

    }

    @Override
    protected void doExecute(WorkflowExecutorContext context) {

        // 初始化上下状态
        context.setTaskBatchStatus(JobTaskBatchStatus.SUCCESS);
        context.setOperationReason(JobOperationReason.NONE);
        context.setJobTaskStatus(JobTaskStatus.SUCCESS);

        if (WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(context.getParentOperationReason())) {
            // 针对无需处理的批次直接新增一个记录
            context.setTaskBatchStatus(JobTaskBatchStatus.CANCEL);
            context.setOperationReason(JobOperationReason.WORKFLOW_NODE_NO_REQUIRED);
            context.setJobTaskStatus(JobTaskStatus.CANCEL);
        } else if (!context.getWorkflowNodeStatus()) {
            context.setTaskBatchStatus(JobTaskBatchStatus.CANCEL);
            context.setOperationReason(JobOperationReason.WORKFLOW_NODE_CLOSED_SKIP_EXECUTION);
            context.setJobTaskStatus(JobTaskStatus.CANCEL);
        } else {
            invokeCallback(context);
        }

    }

    private void invokeCallback(WorkflowExecutorContext context) {

        CallbackConfig decisionConfig = JSON.parseObject(context.getNodeInfo(), CallbackConfig.class);
        String message = StrUtil.EMPTY;
        String result = null;

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set(SystemConstants.SECRET, decisionConfig.getSecret());
        requestHeaders.setContentType(MediaType.valueOf(decisionConfig.getContentType().getMediaType()));
        // 设置回调超时时间
        requestHeaders.set(RequestInterceptor.TIMEOUT_TIME, CALLBACK_TIMEOUT);

        CallbackParamsDTO callbackParamsDTO = new CallbackParamsDTO();
        callbackParamsDTO.setWfContext(context.getWfContext());

        try {
            Map<String, String> uriVariables = new HashMap<>();
            uriVariables.put(SystemConstants.SECRET, decisionConfig.getSecret());

            ResponseEntity<String> response = buildRetryer(decisionConfig).call(
                    () -> restTemplate.exchange(decisionConfig.getWebhook(), HttpMethod.POST,
                            new HttpEntity<>(callbackParamsDTO, requestHeaders), String.class, uriVariables));

            result = response.getBody();
            SilenceJobLog.LOCAL.info("回调结果. webHook:[{}]，结果: [{}]", decisionConfig.getWebhook(), result);
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("回调异常. webHook:[{}]，参数: [{}]", decisionConfig.getWebhook(),
                    context.getWfContext(), e);

            context.setTaskBatchStatus(JobTaskBatchStatus.FAIL);
            context.setOperationReason(JobOperationReason.WORKFLOW_CALLBACK_NODE_EXECUTION_ERROR);
            context.setJobTaskStatus(JobTaskStatus.FAIL);

            Throwable throwable = e;
            if (e.getClass().isAssignableFrom(RetryException.class)) {
                RetryException re = (RetryException) e;
                throwable = re.getLastFailedAttempt().getExceptionCause();
            }

            message = throwable.getMessage();
            var workflowTaskFailAlarmEventDTO = new WorkflowTaskFailAlarmEventDTO();
            workflowTaskFailAlarmEventDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
            workflowTaskFailAlarmEventDTO.setNotifyScene(JobNotifyScene.WORKFLOW_TASK_ERROR);
            workflowTaskFailAlarmEventDTO.setReason(message);
            SilenceSpringContext.getContext().publishEvent(new WorkflowTaskFailAlarmEvent(workflowTaskFailAlarmEventDTO));
        }

        context.setEvaluationResult(result);
        context.setLogMessage(message);

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
        jobLogMetaDTO.setJobId(SystemConstants.CALLBACK_JOB_ID);
        jobLogMetaDTO.setTaskId(jobTask.getId());
        if (jobTaskBatch.getTaskBatchStatus().equals(JobTaskBatchStatus.SUCCESS)) {

            SilenceJobLog.REMOTE.info("节点[{}]回调成功.\n回调参数:{} \n回调结果:[{}] <|>{}<|>",
                    context.getWorkflowNodeId(), context.getWfContext(), context.getEvaluationResult(), jobLogMetaDTO);
        } else if (jobTaskBatch.getTaskBatchStatus().equals(JobTaskBatchStatus.CANCEL)) {

            if (WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(context.getParentOperationReason())) {
                SilenceJobLog.REMOTE.warn("节点[{}]取消回调. 取消原因: 当前任务无需处理 <|>{}<|>",
                        context.getWorkflowNodeId(), jobLogMetaDTO);
            } else {
                SilenceJobLog.REMOTE.warn("节点[{}]取消回调. 取消原因: 任务状态已关闭 <|>{}<|>",
                        context.getWorkflowNodeId(), jobLogMetaDTO);
            }

        } else {
            SilenceJobLog.REMOTE.error("节点[{}]回调失败.\n失败原因:{} <|>{}<|>",
                    context.getWorkflowNodeId(),
                    context.getLogMessage(), jobLogMetaDTO);
        }
    }
}
