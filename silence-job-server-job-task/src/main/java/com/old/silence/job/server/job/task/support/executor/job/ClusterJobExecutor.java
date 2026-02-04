package com.old.silence.job.server.job.task.support.executor.job;

import cn.hutool.core.util.StrUtil;
import org.apache.pekko.actor.ActorRef;
import org.springframework.stereotype.Component;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.common.util.ClientInfoUtils;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.job.task.dto.RealJobExecutorDTO;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.util.List;


@Component
public class ClusterJobExecutor extends AbstractJobExecutor {

    @Override
    public JobTaskType getTaskInstanceType() {
        return JobTaskType.CLUSTER;
    }

    @Override
    protected void doExecute(JobExecutorContext context) {

        // 调度客户端
        List<JobTask> taskList = context.getTaskList();
        if (CollectionUtils.isEmpty(taskList)) {
            throw new SilenceJobServerException("No executable job task");
        }

        JobTask jobTask = taskList.get(0);
        if (StrUtil.isBlank(jobTask.getClientInfo())) {
            return;
        }
        RealJobExecutorDTO realJobExecutor = JobTaskConverter.INSTANCE.toRealJobExecutorDTO(context, jobTask);
        realJobExecutor.setClientId(ClientInfoUtils.clientId(jobTask.getClientInfo()));
        ActorRef actorRef = ActorGenerator.jobRealTaskExecutorActor();
        actorRef.tell(realJobExecutor, actorRef);

    }

}
