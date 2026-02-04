package com.old.silence.job.server.job.task.support.stop;

import kotlin.Result;
import org.apache.pekko.actor.AbstractActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.common.client.dto.StopJobDTO;
import com.old.silence.job.common.model.ApiResult;
import com.old.silence.job.server.common.cache.CacheRegisterTable;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;
import com.old.silence.job.server.common.rpc.client.RequestBuilder;
import com.old.silence.job.server.job.task.client.JobRpcClient;
import com.old.silence.job.server.job.task.dto.RealStopTaskInstanceDTO;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.util.Objects;


@Component(ActorGenerator.JOB_REAL_STOP_TASK_INSTANCE_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)

public class RealStopTaskActor extends AbstractActor {


    private static final Logger log = LoggerFactory.getLogger(RealStopTaskActor.class);

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RealStopTaskInstanceDTO.class, realStopTaskInstanceDTO -> {
            try {
                doStop(realStopTaskInstanceDTO);
            } catch (Exception e) {
                log.error("停止任务执行失败. [{}]", JSON.toJSONString(realStopTaskInstanceDTO), e);
            }
        }).build();
    }

    private void doStop(RealStopTaskInstanceDTO realStopTaskInstanceDTO) {

        // 检查客户端是否存在
        RegisterNodeInfo registerNodeInfo = CacheRegisterTable.getServerNode(
                realStopTaskInstanceDTO.getGroupName(),
                realStopTaskInstanceDTO.getNamespaceId(),
                realStopTaskInstanceDTO.getClientId());
        if (Objects.nonNull(registerNodeInfo)) {
            // 不用关心停止的结果，若服务端尝试终止失败,客户端会兜底进行关闭
            requestClient(realStopTaskInstanceDTO, registerNodeInfo);
        }
    }

    private ApiResult<Boolean> requestClient(RealStopTaskInstanceDTO realStopTaskInstanceDTO, RegisterNodeInfo registerNodeInfo) {
        JobRpcClient rpcClient = RequestBuilder.<JobRpcClient, Result>newBuilder()
                .nodeInfo(registerNodeInfo)
                .failRetry(Boolean.TRUE)
                .retryTimes(3)
                .retryInterval(1)
                .client(JobRpcClient.class)
                .build();

        StopJobDTO stopJobDTO = new StopJobDTO();
        stopJobDTO.setTaskBatchId(realStopTaskInstanceDTO.getTaskBatchId());
        stopJobDTO.setJobId(realStopTaskInstanceDTO.getJobId());
        stopJobDTO.setGroupName(realStopTaskInstanceDTO.getGroupName());
        return rpcClient.stop(stopJobDTO);
    }
}
