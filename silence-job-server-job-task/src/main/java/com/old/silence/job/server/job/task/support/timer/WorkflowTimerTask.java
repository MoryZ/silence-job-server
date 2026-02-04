package com.old.silence.job.server.job.task.support.timer;

import io.netty.util.Timeout;
import org.apache.pekko.actor.ActorRef;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.TimerTask;
import com.old.silence.job.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.old.silence.job.server.job.task.dto.WorkflowTimerTaskDTO;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.text.MessageFormat;
import java.time.Instant;


public class WorkflowTimerTask implements TimerTask<String> {
    public static final String IDEMPOTENT_KEY_PREFIX = "workflow_{0}";

    private final WorkflowTimerTaskDTO workflowTimerTaskDTO;

    public WorkflowTimerTask(WorkflowTimerTaskDTO workflowTimerTaskDTO) {
        this.workflowTimerTaskDTO = workflowTimerTaskDTO;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        // 执行任务调度
        SilenceJobLog.LOCAL.debug("开始执行任务调度. 当前时间:[{}] taskId:[{}]", Instant.now(), workflowTimerTaskDTO.getWorkflowTaskBatchId());

        try {

            WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
            taskExecuteDTO.setWorkflowTaskBatchId(workflowTimerTaskDTO.getWorkflowTaskBatchId());
            taskExecuteDTO.setTaskExecutorScene(workflowTimerTaskDTO.getTaskExecutorScene());
            taskExecuteDTO.setParentId(SystemConstants.ROOT);
            ActorRef actorRef = ActorGenerator.workflowTaskExecutorActor();
            actorRef.tell(taskExecuteDTO, actorRef);

        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("任务调度执行失败", e);
        }
    }

    @Override
    public String idempotentKey() {
        return MessageFormat.format(IDEMPOTENT_KEY_PREFIX, workflowTimerTaskDTO.getWorkflowTaskBatchId());
    }
}
