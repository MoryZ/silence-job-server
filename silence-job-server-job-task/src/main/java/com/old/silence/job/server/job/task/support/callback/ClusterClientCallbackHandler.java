package com.old.silence.job.server.job.task.support.callback;

import org.apache.pekko.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;
import com.old.silence.job.server.common.handler.ClientNodeAllocateHandler;
import com.old.silence.job.server.common.util.ClientInfoUtils;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowTaskBatchDao;
import com.old.silence.job.server.job.task.dto.JobExecutorResultDTO;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.util.Objects;


@Component

public class ClusterClientCallbackHandler extends AbstractClientCallbackHandler {


    private static final Logger log = LoggerFactory.getLogger(ClusterClientCallbackHandler.class);
    private final ClientNodeAllocateHandler clientNodeAllocateHandler;

    public ClusterClientCallbackHandler(JobTaskDao jobTaskDao, JobDao jobDao, WorkflowTaskBatchDao workflowTaskBatchDao,
                                        ClientNodeAllocateHandler clientNodeAllocateHandler) {
        super(jobTaskDao, jobDao, workflowTaskBatchDao);
        this.clientNodeAllocateHandler = clientNodeAllocateHandler;
    }

    @Override
    public JobTaskType getTaskInstanceType() {
        return JobTaskType.CLUSTER;
    }

    @Override
    protected String chooseNewClient(ClientCallbackContext context) {

        // 选择重试的节点
        RegisterNodeInfo serverNode = clientNodeAllocateHandler.getServerNode(context.getJobId().toString(),
                context.getGroupName(), context.getNamespaceId(), context.getJob().getRouteKey());
        if (Objects.isNull(serverNode)) {
            log.error("无可执行的客户端信息. jobId:[{}]", context.getJobId());
            return null;
        }

        return ClientInfoUtils.generate(serverNode);
    }

    @Override
    protected void doCallback(ClientCallbackContext context) {

        JobExecutorResultDTO jobExecutorResultDTO = JobTaskConverter.INSTANCE.toJobExecutorResultDTO(context);
        jobExecutorResultDTO.setTaskId(context.getTaskId());
        jobExecutorResultDTO.setMessage(context.getExecuteResult().getMessage());
        jobExecutorResultDTO.setResult(context.getExecuteResult().getResult());
        jobExecutorResultDTO.setTaskType(getTaskInstanceType());

        ActorRef actorRef = ActorGenerator.jobTaskExecutorResultActor();
        actorRef.tell(jobExecutorResultDTO, actorRef);

    }

}
