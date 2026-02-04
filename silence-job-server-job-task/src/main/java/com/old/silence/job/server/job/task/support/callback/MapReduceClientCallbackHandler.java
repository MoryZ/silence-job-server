package com.old.silence.job.server.job.task.support.callback;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import org.apache.pekko.actor.ActorRef;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.cache.CacheRegisterTable;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;
import com.old.silence.job.server.common.util.ClientInfoUtils;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowTaskBatchDao;
import com.old.silence.job.server.job.task.dto.JobExecutorResultDTO;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.util.Set;


@Component
public class MapReduceClientCallbackHandler extends AbstractClientCallbackHandler {

    public MapReduceClientCallbackHandler(JobTaskDao jobTaskDao, JobDao jobDao,
                                          WorkflowTaskBatchDao workflowTaskBatchDao) {
        super(jobTaskDao, jobDao, workflowTaskBatchDao);
    }

    @Override
    public JobTaskType getTaskInstanceType() {
        return JobTaskType.MAP_REDUCE;
    }

    @Override
    protected void doCallback(ClientCallbackContext context) {
        JobTask jobTask = jobTaskDao.selectOne(new LambdaQueryWrapper<JobTask>()
                .eq(JobTask::getId, context.getTaskId()));
        Assert.notNull(jobTask, () -> new SilenceJobServerException("job task is null"));

        JobExecutorResultDTO jobExecutorResultDTO = JobTaskConverter.INSTANCE.toJobExecutorResultDTO(context);
        jobExecutorResultDTO.setTaskId(context.getTaskId());
        jobExecutorResultDTO.setMessage(context.getExecuteResult().getMessage());
        jobExecutorResultDTO.setResult(context.getExecuteResult().getResult());
        jobExecutorResultDTO.setTaskType(getTaskInstanceType());
        jobExecutorResultDTO.setIsLeaf(jobTask.getLeaf());
        ActorRef actorRef = ActorGenerator.jobTaskExecutorResultActor();
        actorRef.tell(jobExecutorResultDTO, actorRef);
    }

    @Override
    protected String chooseNewClient(ClientCallbackContext context) {
        Set<RegisterNodeInfo> nodes = CacheRegisterTable.getServerNodeSet(context.getGroupName(), context.getNamespaceId());
        if (CollectionUtils.isEmpty(nodes)) {
            SilenceJobLog.LOCAL.error("无可执行的客户端信息. jobId:[{}]", context.getJobId());
            return null;
        }

        RegisterNodeInfo serverNode = RandomUtil.randomEle(nodes.toArray(new RegisterNodeInfo[0]));
        return ClientInfoUtils.generate(serverNode);
    }
}
