package com.old.silence.job.server.job.task.support.callback;

import cn.hutool.core.util.RandomUtil;
import org.apache.pekko.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.common.cache.CacheRegisterTable;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;
import com.old.silence.job.server.common.util.ClientInfoUtils;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowTaskBatchDao;
import com.old.silence.job.server.job.task.dto.JobExecutorResultDTO;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.util.Set;


@Component

public class ShardingClientCallbackHandler extends AbstractClientCallbackHandler {


    private static final Logger log = LoggerFactory.getLogger(ShardingClientCallbackHandler.class);

    public ShardingClientCallbackHandler(JobTaskDao jobTaskDao, JobDao jobDao, WorkflowTaskBatchDao workflowTaskBatchDao) {
        super(jobTaskDao, jobDao, workflowTaskBatchDao);
    }

    @Override
    public JobTaskType getTaskInstanceType() {
        return JobTaskType.SHARDING;
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

        RegisterNodeInfo serverNode = RandomUtil.randomEle(nodes.toArray(new RegisterNodeInfo[0]));
        return ClientInfoUtils.generate(serverNode);
    }
}
