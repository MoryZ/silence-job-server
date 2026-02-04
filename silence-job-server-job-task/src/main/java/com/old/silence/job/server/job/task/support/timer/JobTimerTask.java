package com.old.silence.job.server.job.task.support.timer;

import io.netty.util.Timeout;
import org.apache.pekko.actor.ActorRef;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.TimerTask;
import com.old.silence.job.server.job.task.dto.JobTimerTaskDTO;
import com.old.silence.job.server.job.task.dto.TaskExecuteDTO;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.text.MessageFormat;
import java.time.Instant;


public class JobTimerTask implements TimerTask<String> {
    public static final String IDEMPOTENT_KEY_PREFIX = "job_{0}";
    private final JobTimerTaskDTO jobTimerTaskDTO;

    public JobTimerTask(JobTimerTaskDTO jobTimerTaskDTO) {
        this.jobTimerTaskDTO = jobTimerTaskDTO;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        // 执行任务调度
        SilenceJobLog.LOCAL.debug("开始执行任务调度. 当前时间:[{}] taskId:[{}]", Instant.now(), jobTimerTaskDTO.getTaskBatchId());

        try {
            TaskExecuteDTO taskExecuteDTO = new TaskExecuteDTO();
            taskExecuteDTO.setTaskBatchId(jobTimerTaskDTO.getTaskBatchId());
            taskExecuteDTO.setJobId(jobTimerTaskDTO.getJobId());
            taskExecuteDTO.setTaskExecutorScene(jobTimerTaskDTO.getTaskExecutorScene());
            taskExecuteDTO.setWorkflowTaskBatchId(jobTimerTaskDTO.getWorkflowTaskBatchId());
            taskExecuteDTO.setWorkflowNodeId(jobTimerTaskDTO.getWorkflowNodeId());
            taskExecuteDTO.setTmpArgsStr(jobTimerTaskDTO.getTmpArgsStr());
            ActorRef actorRef = ActorGenerator.jobTaskExecutorActor();
            actorRef.tell(taskExecuteDTO, actorRef);

        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("任务调度执行失败", e);
        }
    }

    @Override
    public String idempotentKey() {
        return MessageFormat.format(IDEMPOTENT_KEY_PREFIX, jobTimerTaskDTO.getTaskBatchId());
    }
}
