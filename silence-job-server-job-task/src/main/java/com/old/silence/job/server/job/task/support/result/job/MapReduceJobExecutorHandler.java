package com.old.silence.job.server.job.task.support.result.job;

import org.apache.pekko.actor.ActorRef;
import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.common.enums.MapReduceStage;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.infrastructure.persistence.dao.GroupConfigDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.job.task.dto.ReduceTaskDTO;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.job.task.support.handler.WorkflowBatchHandler;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.util.Objects;

import static com.old.silence.job.common.enums.MapReduceStage.MAP;
import static com.old.silence.job.common.enums.MapReduceStage.MERGE_REDUCE;
import static com.old.silence.job.common.enums.MapReduceStage.REDUCE;

@Component
public class MapReduceJobExecutorHandler extends AbstractJobExecutorResultHandler {

    public MapReduceJobExecutorHandler(
            final JobTaskDao jobTaskDao,
            final JobTaskBatchDao jobTaskBatchDao,
            final WorkflowBatchHandler workflowBatchHandler,
            final GroupConfigDao groupConfigDao) {
        super(jobTaskDao, jobTaskBatchDao, workflowBatchHandler, groupConfigDao);
    }

    @Override
    public JobTaskType getTaskInstanceType() {
        return JobTaskType.MAP_REDUCE;
    }

    @Override
    protected void doHandleSuccess(JobExecutorResultContext context) {
        // 判断是否需要创建Reduce任务
        context.setCreateReduceTask(needReduceTask(context));
    }

    @Override
    protected void doHandleStop(JobExecutorResultContext context) {

    }

    @Override
    protected void doHandleFail(JobExecutorResultContext context) {

    }

    @Override
    protected boolean updateStatus(JobExecutorResultContext context, JobTaskBatchStatus status) {
        if (context.isCreateReduceTask()) {
            // 此时中断批次完成，需要开启reduce任务
            return false;
        }
        return super.updateStatus(context, status);
    }

    /**
     * 若需要执行reduce则返回false 不需要更新批次状态， 否则需要更新批次状态
     *
     * @param context 需要执行批次完成所需的参数信息
     * @return true-需要reduce false-不需要reduce
     */
    private boolean needReduceTask(JobExecutorResultContext context) {
        MapReduceStage mrStage;

        int reduceCount = 0;
        int mapCount = 0;
        for (JobTask jobTask : context.getJobTaskList()) {
            if (Objects.isNull(jobTask.getMrStage())) {
                continue;
            }

            // 存在MERGE_REDUCE任务了不需要生成
            if (MERGE_REDUCE == jobTask.getMrStage()) {
                return false;
            }

            // REDUCE任务累加
            if (REDUCE == jobTask.getMrStage()) {
                reduceCount++;
                continue;
            }

            // MAP任务累加
            if (MAP == jobTask.getMrStage()) {
                mapCount++;
            }
        }

        // 若存在2个以上的reduce任务则开启merge reduce任务
        if (reduceCount > 1) {
            mrStage = MERGE_REDUCE;
        } else if (mapCount == context.getJobTaskList().size()) {
            // 若都是MAP任务则开启Reduce任务
            mrStage = REDUCE;
        } else {
            // 若既不是MAP也是不REDUCE则是其他类型的任务，直接返回即可
            return false;
        }

        // 开启reduce or mergeReduce阶段
        try {
            ReduceTaskDTO reduceTaskDTO = JobTaskConverter.INSTANCE.toReduceTaskDTO(context);
            reduceTaskDTO.setMrStage(mrStage);
            ActorRef actorRef = ActorGenerator.jobReduceActor();
            actorRef.tell(reduceTaskDTO, actorRef);
            return true;
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("tell reduce actor error", e);
        }

        return false;
    }

    @Override
    protected void openNextWorkflowNode(JobExecutorResultContext context) {
        if (context.isCreateReduceTask()) {
            // 任务暂未完成，无需开启后续节点；更新上下文
            return;
        }
        super.openNextWorkflowNode(context);
    }
}
