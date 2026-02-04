package com.old.silence.job.server.retry.task.support.timer;

import io.netty.util.Timeout;
import org.apache.pekko.actor.ActorRef;
import com.old.silence.job.server.common.pekko.ActorGenerator;
import com.old.silence.job.server.retry.task.dto.RetryTaskExecuteDTO;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;

import java.text.MessageFormat;


public class RetryTimerTask extends AbstractTimerTask {
    public static final String IDEMPOTENT_KEY_PREFIX = "retry_task_{0}";

    private final RetryTimerContext context;

    @Override
    public void doRun(final Timeout timeout) {

        RetryTaskExecuteDTO taskExecuteDTO =  RetryTaskConverter.INSTANCE.toRetryTaskExecuteDTO(context);
        // 执行阶段
        ActorRef actorRef = ActorGenerator.retryTaskExecutorActor();
        actorRef.tell(taskExecuteDTO, actorRef);

    }

    public RetryTimerTask(RetryTimerContext context) {
        this.context = context;
        super.retryId = context.getRetryId();
        super.retryTaskId = context.getRetryTaskId();
    }

    @Override
    public String idempotentKey() {
        return MessageFormat.format(IDEMPOTENT_KEY_PREFIX, context.getRetryTaskId());
    }
}
