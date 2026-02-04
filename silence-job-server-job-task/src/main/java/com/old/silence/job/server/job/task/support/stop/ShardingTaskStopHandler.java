package com.old.silence.job.server.job.task.support.stop;

import org.apache.pekko.actor.ActorRef;
import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.common.util.ClientInfoUtils;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.job.task.dto.RealStopTaskInstanceDTO;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.common.pekko.ActorGenerator;


@Component

public class ShardingTaskStopHandler extends AbstractJobTaskStopHandler {

    protected ShardingTaskStopHandler(JobTaskDao jobTaskDao, JobTaskBatchDao jobTaskBatchDao) {
        super(jobTaskDao, jobTaskBatchDao);
    }

    @Override
    public JobTaskType getTaskType() {
        return JobTaskType.SHARDING;
    }

    @Override
    public void doStop(TaskStopJobContext context) {

        for (JobTask jobTask : context.getJobTasks()) {
            RealStopTaskInstanceDTO taskInstanceDTO = JobTaskConverter.INSTANCE.toRealStopTaskInstanceDTO(context);
            taskInstanceDTO.setClientId(ClientInfoUtils.clientId(jobTask.getClientInfo()));

            ActorRef actorRef = ActorGenerator.jobRealStopTaskInstanceActor();
            actorRef.tell(taskInstanceDTO, actorRef);
        }

    }

}
