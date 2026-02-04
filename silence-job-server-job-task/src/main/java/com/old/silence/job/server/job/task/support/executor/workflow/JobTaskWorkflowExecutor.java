package com.old.silence.job.server.job.task.support.executor.workflow;

import org.apache.pekko.actor.ActorRef;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.common.enums.JobTaskStatus;
import com.old.silence.job.common.enums.WorkflowNodeType;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.dto.JobLogMetaDTO;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.job.task.dto.JobTaskPrepareDTO;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.job.task.support.generator.batch.JobTaskBatchGenerator;
import com.old.silence.job.server.job.task.support.handler.DistributedLockHandler;
import com.old.silence.job.server.job.task.support.handler.WorkflowBatchHandler;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import static com.old.silence.job.common.enums.JobOperationReason.WORKFLOW_NODE_CLOSED_SKIP_EXECUTION;
import static com.old.silence.job.common.enums.JobOperationReason.WORKFLOW_SUCCESSOR_SKIP_EXECUTION;


@Component
public class JobTaskWorkflowExecutor extends AbstractWorkflowExecutor {


    protected JobTaskWorkflowExecutor(DistributedLockHandler distributedLockHandler, JobTaskBatchDao jobTaskBatchDao, JobTaskBatchGenerator jobTaskBatchGenerator, WorkflowBatchHandler workflowBatchHandler, JobTaskDao jobTaskDao, TransactionTemplate transactionTemplate) {
        super(distributedLockHandler, jobTaskBatchDao, jobTaskBatchGenerator, workflowBatchHandler, jobTaskDao, transactionTemplate);
    }

    private static void invokeJobTask(final WorkflowExecutorContext context) {
        // 生成任务批次
        JobTaskPrepareDTO jobTaskPrepare = JobTaskConverter.INSTANCE.toJobTaskPrepare(context.getJob(), context);
        jobTaskPrepare.setNextTriggerAt(DateUtils.toNowMilli() + DateUtils.toNowMilli() % 1000);
        // 执行预处理阶段
        ActorRef actorRef = ActorGenerator.jobTaskPrepareActor();
        actorRef.tell(jobTaskPrepare, actorRef);
    }

    @Override
    public WorkflowNodeType getWorkflowNodeType() {
        return WorkflowNodeType.JOB_TASK;
    }

    @Override
    protected boolean doPreValidate(WorkflowExecutorContext context) {
        return true;
    }

    @Override
    protected void afterExecute(WorkflowExecutorContext context) {

    }

    @Override
    protected void beforeExecute(WorkflowExecutorContext context) {

    }

    @Override
    protected void doExecute(WorkflowExecutorContext context) {

        if (WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(context.getParentOperationReason())) {
            // 针对无需处理的批次直接新增一个记录
            context.setTaskBatchStatus(JobTaskBatchStatus.CANCEL);
            context.setOperationReason(JobOperationReason.WORKFLOW_NODE_NO_REQUIRED);
            context.setJobTaskStatus(JobTaskStatus.CANCEL);

            // 创建批次和任务节点4
            invokeCancelJobTask(context, "当前节点无需处理");
        } else if (!context.getWorkflowNodeStatus()) {
            // 针对无需处理的批次直接新增一个记录
            context.setTaskBatchStatus(JobTaskBatchStatus.CANCEL);
            context.setOperationReason(WORKFLOW_NODE_CLOSED_SKIP_EXECUTION);
            context.setJobTaskStatus(JobTaskStatus.CANCEL);

            // 创建批次和任务节点
            invokeCancelJobTask(context, "任务已关闭");
        } else {
            invokeJobTask(context);
        }

    }

    private void invokeCancelJobTask(final WorkflowExecutorContext context, String cancelReason) {

        JobTaskBatch jobTaskBatch = generateJobTaskBatch(context);
        JobTask jobTask = generateJobTask(context, jobTaskBatch);

        JobLogMetaDTO jobLogMetaDTO = new JobLogMetaDTO();
        jobLogMetaDTO.setNamespaceId(context.getNamespaceId());
        jobLogMetaDTO.setGroupName(context.getGroupName());
        jobLogMetaDTO.setTaskBatchId(jobTaskBatch.getId());
        jobLogMetaDTO.setJobId(context.getJobId());
        jobLogMetaDTO.setTaskId(jobTask.getId());

        SilenceJobLog.REMOTE.warn("节点[{}]已取消任务执行. 取消原因: {}. <|>{}<|>",
                context.getWorkflowNodeId(), cancelReason, jobLogMetaDTO);
    }
}
