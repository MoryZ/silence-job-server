package com.old.silence.job.server.job.task.support.executor.job;

import cn.hutool.core.util.StrUtil;
import org.apache.pekko.actor.ActorRef;
import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.common.util.ClientInfoUtils;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.job.task.dto.RealJobExecutorDTO;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.util.List;


@Component

public class ShardingJobExecutor extends AbstractJobExecutor {

    @Override
    public JobTaskType getTaskInstanceType() {
        return JobTaskType.SHARDING;
    }

    @Override
    protected void doExecute(JobExecutorContext context) {
        List<JobTask> taskList = context.getTaskList();
        for (int i = 0; i < taskList.size(); i++) {
            JobTask jobTask = taskList.get(i);
            if (StrUtil.isBlank(jobTask.getClientInfo())) {
                return;
            }
            RealJobExecutorDTO realJobExecutor = JobTaskConverter.INSTANCE.toRealJobExecutorDTO(context, jobTask);
            realJobExecutor.setClientId(ClientInfoUtils.clientId(jobTask.getClientInfo()));
            realJobExecutor.setShardingIndex(i);
            realJobExecutor.setShardingTotal(taskList.size());
            ActorRef actorRef = ActorGenerator.jobRealTaskExecutorActor();
            actorRef.tell(realJobExecutor, actorRef);
        }

    }
}
