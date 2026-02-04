package com.old.silence.job.server.job.task.support.callback;

import org.apache.pekko.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Sets;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.common.pekko.ActorGenerator;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.server.common.cache.CacheRegisterTable;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;
import com.old.silence.job.server.common.util.ClientInfoUtils;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowTaskBatchDao;
import com.old.silence.job.server.job.task.dto.JobExecutorResultDTO;
import com.old.silence.job.server.job.task.support.JobTaskConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@Component

public class BroadcastClientCallbackHandler extends AbstractClientCallbackHandler {


    private static final Logger log = LoggerFactory.getLogger(BroadcastClientCallbackHandler.class);

    public BroadcastClientCallbackHandler(JobTaskDao jobTaskDao, JobDao jobDao, WorkflowTaskBatchDao workflowTaskBatchDao) {
        super(jobTaskDao, jobDao, workflowTaskBatchDao);
    }

    @Override
    public JobTaskType getTaskInstanceType() {
        return JobTaskType.BROADCAST;
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

    @Override
    protected String chooseNewClient(ClientCallbackContext context) {
        Set<RegisterNodeInfo> nodes = CacheRegisterTable.getServerNodeSet(context.getGroupName(), context.getNamespaceId());
        if (CollectionUtils.isEmpty(nodes)) {
            log.error("无可执行的客户端信息. jobId:[{}]", context.getJobId());
            return null;
        }

        JobTask jobTask = context.getJobTask();
        String clientInfo = jobTask.getClientInfo();
        String clientId = ClientInfoUtils.clientId(clientInfo);
        RegisterNodeInfo serverNode = CacheRegisterTable.getServerNode(context.getGroupName(), context.getNamespaceId(), clientId);
        if (Objects.isNull(serverNode)) {
            List<JobTask> jobTasks = super.jobTaskDao.selectList(new LambdaQueryWrapper<JobTask>()
                    .eq(JobTask::getTaskBatchId, context.getTaskBatchId()));

            Set<String> clientIdList = StreamUtils.toSet(jobTasks, jobTask1 -> ClientInfoUtils.clientId(jobTask1.getClientInfo()));
            Set<String> remoteClientIdSet = StreamUtils.toSet(nodes, RegisterNodeInfo::getHostId);
            Sets.SetView<String> diff = Sets.difference(remoteClientIdSet, clientIdList);

            String newClientId = CollectionUtils.firstElement(new ArrayList<>(diff)).orElseThrow();
            RegisterNodeInfo registerNodeInfo = CacheRegisterTable.getServerNode(context.getGroupName(), context.getNamespaceId(), newClientId);
            if (Objects.isNull(registerNodeInfo)) {
                // 如果找不到新的客户端信息，则返回原来的客户端信息
                return clientInfo;
            }

            return ClientInfoUtils.generate(registerNodeInfo);
        }

        return clientInfo;
    }
}
