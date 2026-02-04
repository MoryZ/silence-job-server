package com.old.silence.job.server.retry.task.support.handler;

import org.apache.pekko.actor.ActorRef;
import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.RetryTaskStatus;
import com.old.silence.job.server.common.pekko.ActorGenerator;
import com.old.silence.job.server.retry.task.dto.RequestRetryExecutorDTO;
import com.old.silence.job.server.retry.task.dto.RetryExecutorResultDTO;
import com.old.silence.job.server.retry.task.dto.TaskStopJobDTO;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;


@Component
public class RetryTaskStopHandler {
    /**
     * 执行停止任务
     *
     */
    public void stop(TaskStopJobDTO stopJobDTO) {

        RequestRetryExecutorDTO executorDTO = RetryTaskConverter.INSTANCE.toRealRetryExecutorDTO(stopJobDTO);
        ActorRef actorRef = ActorGenerator.stopRetryTaskActor();
        actorRef.tell(executorDTO, actorRef);

        // 更新结果为失败
        doHandleResult(stopJobDTO);
    }

    private static void doHandleResult(TaskStopJobDTO stopJobDTO) {
        if (!stopJobDTO.isNeedUpdateTaskStatus()) {
            return;
        }
        RetryExecutorResultDTO executorResultDTO = RetryTaskConverter.INSTANCE.toRetryExecutorResultDTO(stopJobDTO);
        executorResultDTO.setExceptionMsg(stopJobDTO.getMessage());
        executorResultDTO.setTaskStatus(RetryTaskStatus.FAIL);
        executorResultDTO.setOperationReason(stopJobDTO.getOperationReason());
        ActorRef actorRef = ActorGenerator.retryTaskExecutorResultActor();
        actorRef.tell(executorResultDTO, actorRef);
    }


}
