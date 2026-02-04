package com.old.silence.job.server.retry.task.support.dispatch;

import org.apache.pekko.actor.AbstractActor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.pekko.ActorGenerator;
import com.old.silence.job.server.retry.task.dto.RetryExecutorResultDTO;
import com.old.silence.job.server.retry.task.support.RetryResultHandler;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;
import com.old.silence.job.server.retry.task.support.result.RetryResultContext;

import java.util.List;


@Component(ActorGenerator.RETRY_EXECUTOR_RESULT_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)

public class RetryResultActor extends AbstractActor {
    private final List<RetryResultHandler> retryResultHandlers;

    public RetryResultActor(List<RetryResultHandler> retryResultHandlers) {
        this.retryResultHandlers = retryResultHandlers;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryExecutorResultDTO.class, result -> {
            try {
                doResult(result);
            } catch (Exception e) {
                SilenceJobLog.LOCAL.error("Result processing exception. [{}]", result, e);
            }
        }).build();
    }

    private void doResult(RetryExecutorResultDTO result) {
        RetryResultContext context = RetryTaskConverter.INSTANCE.toRetryResultContext(result);
        for (RetryResultHandler retryResultHandler : retryResultHandlers) {
            if (retryResultHandler.supports(context)) {
                retryResultHandler.handle(context);
            }
        }
    }
}
