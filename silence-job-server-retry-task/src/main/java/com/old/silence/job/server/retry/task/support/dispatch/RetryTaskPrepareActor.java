package com.old.silence.job.server.retry.task.support.dispatch;

import org.apache.pekko.actor.AbstractActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.RetryTaskExecutorSceneEnum;
import com.old.silence.job.server.domain.model.RetryTask;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryTaskDao;
import com.old.silence.job.server.retry.task.dto.RetryTaskPrepareDTO;
import com.old.silence.job.server.retry.task.support.RetryPrePareHandler;

import java.util.List;
import java.util.Objects;

import static com.old.silence.job.common.enums.RetryTaskStatus.NOT_COMPLETE;
import static com.old.silence.job.common.enums.RetryTaskStatus.SUCCESS;
import static com.old.silence.job.server.common.pekko.ActorGenerator.RETRY_TASK_PREPARE_ACTOR;


@Component(RETRY_TASK_PREPARE_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RetryTaskPrepareActor extends AbstractActor {

    private static final Logger log = LoggerFactory.getLogger(RetryTaskPrepareActor.class);
    private final List<RetryPrePareHandler> retryPrePareHandlers;
    private final RetryTaskDao retryTaskDao;

    public RetryTaskPrepareActor(List<RetryPrePareHandler> retryPrePareHandlers, RetryTaskDao retryTaskDao) {
        this.retryPrePareHandlers = retryPrePareHandlers;
        this.retryTaskDao = retryTaskDao;
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryTaskPrepareDTO.class, prepareDTO -> {

            try {
                doPrepare(prepareDTO);
            } catch (Exception e) {
                log.error("预处理节点异常", e);
            } finally {
                getContext().stop(getSelf());
            }

        }).build();
    }

    /**
     * 对数据进行预处理
     *
     * @param prepareDTO RetryTaskPrepareDTO
     */
    private void doPrepare(RetryTaskPrepareDTO prepareDTO) {

        List<RetryTask> retryTasks = retryTaskDao.selectList(
                new LambdaQueryWrapper<RetryTask>()
                        .eq(RetryTask::getRetryId, prepareDTO.getRetryId())
                        .in(RetryTask::getTaskStatus, NOT_COMPLETE)
                        .orderByAsc(RetryTask::getRetryId)
        );

        if (CollectionUtils.isEmpty(retryTasks)
                || Objects.isNull(prepareDTO.getRetryTaskExecutorScene())
                || RetryTaskExecutorSceneEnum.MANUAL_RETRY.getScene() == prepareDTO.getRetryTaskExecutorScene()) {
            RetryTask retryTask = new RetryTask();
            retryTask.setTaskStatus(SUCCESS);
            retryTasks = Lists.newArrayList(retryTask);
        }

        boolean onlyTimeoutCheck = false;
        for (RetryTask retryTask : retryTasks) {
            prepareDTO.setRetryTaskId(retryTask.getId());
            prepareDTO.setOnlyTimeoutCheck(onlyTimeoutCheck);
            for (RetryPrePareHandler retryPrePareHandler : retryPrePareHandlers) {
                if (retryPrePareHandler.matches(retryTask.getTaskStatus())) {
                    retryPrePareHandler.handle(prepareDTO);
                    break;
                }
            }

            // 当存在大量待处理任务时，除了第一个任务需要执行阻塞策略，其他任务只做任务检查
            onlyTimeoutCheck = true;
        }
    }
}
