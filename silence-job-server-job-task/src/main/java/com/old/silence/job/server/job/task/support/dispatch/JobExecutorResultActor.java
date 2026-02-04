package com.old.silence.job.server.job.task.support.dispatch;

import cn.hutool.core.lang.Assert;
import org.apache.pekko.actor.AbstractActor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.job.task.dto.CompleteJobBatchDTO;
import com.old.silence.job.server.job.task.dto.JobExecutorResultDTO;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.job.task.support.handler.JobTaskBatchHandler;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.util.Objects;


@Component(ActorGenerator.JOB_EXECUTOR_RESULT_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JobExecutorResultActor extends AbstractActor {
    private final JobTaskDao jobTaskDao;
    private final JobTaskBatchHandler jobTaskBatchHandler;

    public JobExecutorResultActor(JobTaskDao jobTaskDao, JobTaskBatchHandler jobTaskBatchHandler) {
        this.jobTaskDao = jobTaskDao;
        this.jobTaskBatchHandler = jobTaskBatchHandler;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(JobExecutorResultDTO.class, result -> {
            SilenceJobLog.LOCAL.debug("更新任务状态. 参数:[{}]", JSON.toJSONString(result));
            try {
                Assert.notNull(result.getTaskId(), () -> new SilenceJobServerException("taskId can not be null"));
                Assert.notNull(result.getJobId(), () -> new SilenceJobServerException("jobId can not be null"));
                Assert.notNull(result.getTaskBatchId(), () -> new SilenceJobServerException("taskBatchId can not be null"));
                Assert.notNull(result.getTaskType(), () -> new SilenceJobServerException("taskType can not be null"));

                JobTask jobTask = new JobTask();
                jobTask.setTaskStatus(result.getTaskStatus());
                jobTask.setWfContext(result.getWfContext());
                if (Objects.nonNull(result.getResult())) {
                    if (result.getResult() instanceof String) {
                        jobTask.setResultMessage((String) result.getResult());
                    } else {
                        jobTask.setResultMessage(JSON.toJSONString(result.getResult()));
                    }
                }

                Assert.isTrue(1 == jobTaskDao.update(jobTask,
                                new LambdaUpdateWrapper<JobTask>().eq(JobTask::getId, result.getTaskId())),
                        () -> new SilenceJobServerException("更新任务实例失败"));

                // 除MAP和MAP_REDUCE 任务之外，其他任务都是叶子节点
                if (Objects.nonNull(result.getIsLeaf()) && !result.getIsLeaf()) {
                    return;
                }

                tryCompleteAndStop(result);
            } catch (Exception e) {
                SilenceJobLog.LOCAL.error(" job executor result exception. [{}]", result, e);
            } finally {
                getContext().stop(getSelf());
            }

        }).build();

    }

    private void tryCompleteAndStop(JobExecutorResultDTO jobExecutorResultDTO) {
        CompleteJobBatchDTO completeJobBatchDTO = JobTaskConverter.INSTANCE.toCompleteJobBatchDTO(jobExecutorResultDTO);
        jobTaskBatchHandler.handleResult(completeJobBatchDTO);
    }
}
