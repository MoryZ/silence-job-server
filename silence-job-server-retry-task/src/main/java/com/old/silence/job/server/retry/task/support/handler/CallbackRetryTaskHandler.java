package com.old.silence.job.server.retry.task.support.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.common.enums.RetryStatus;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.server.common.WaitStrategy;
import com.old.silence.job.server.common.strategy.WaitStrategies.WaitStrategyContext;
import com.old.silence.job.server.common.strategy.WaitStrategies.WaitStrategyEnum;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.domain.model.Retry;
import com.old.silence.job.server.domain.model.RetrySceneConfig;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryDao;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;

import java.time.Instant;
import java.util.Objects;


/**
 * 回调数据处理器
 *
 */
@Component

public class CallbackRetryTaskHandler {

    private static final Logger log = LoggerFactory.getLogger(CallbackRetryTaskHandler.class);
    private final RetryDao retryDao;

    public CallbackRetryTaskHandler(RetryDao retryDao) {
        this.retryDao = retryDao;
    }

    /**
     * 创建回调数据
     *
     * @param parentRetry            {@link Retry} 重试任务数据
     * @param retrySceneConfig      {@link RetrySceneConfig} 回调场景配置
     */
    public void create(Retry parentRetry, RetrySceneConfig retrySceneConfig) {
        if (!SystemTaskType.RETRY.equals(parentRetry.getTaskType())) {
            return;
        }

        if (Objects.isNull(retrySceneConfig.getCbStatus()) || retrySceneConfig.getCbStatus()) {
            return;
        }

        Retry callbackRetry = RetryTaskConverter.INSTANCE.toRetryTask(parentRetry);
        callbackRetry.setTaskType(SystemTaskType.CALLBACK);
        callbackRetry.setParentId(parentRetry.getId());
        callbackRetry.setRetryStatus(RetryStatus.RUNNING);
        callbackRetry.setRetryCount(0);
        callbackRetry.setCreatedDate(Instant.now());
        callbackRetry.setUpdatedDate(Instant.now());

        String triggerInterval = retrySceneConfig.getCbTriggerInterval();
        WaitStrategy waitStrategy = WaitStrategyEnum.getWaitStrategy(retrySceneConfig.getCbTriggerType().getValue());
        WaitStrategyContext waitStrategyContext = new WaitStrategyContext();
        waitStrategyContext.setNextTriggerAt(DateUtils.toNowMilli());
        waitStrategyContext.setDelayLevel(1);
        waitStrategyContext.setTriggerInterval(String.valueOf(triggerInterval));

        callbackRetry.setNextTriggerAt(waitStrategy.computeTriggerTime(waitStrategyContext));

        try {
            Assert.isTrue(1 == retryDao.insert(callbackRetry),
                    () -> new SilenceJobServerException("failed to report data"));
        } catch (DuplicateKeyException e) {
            log.warn("回调数据重复新增. [{}]", JSON.toJSONString(callbackRetry));
        }
    }

//    /**
//     * 生成回调数据
//     *
//     * @param uniqueId 重试任务uniqueId
//     * @return 回调任务uniqueId
//     */
//    public String generatorCallbackUniqueId(String uniqueId) {
//        // eg: CB_202307180949471
//        FormattingTuple callbackUniqueId = MessageFormatter.arrayFormat(CALLBACK_UNIQUE_ID_RULE,
//                new Object[]{systemProperties.getCallback().getPrefix(), uniqueId});
//
//        return callbackUniqueId.getMessage();
//    }

    /**
     * 获取重试任务uniqueId
     *
     * @param callbackTaskUniqueId 回调任务uniqueId
     * @return 重试任务uniqueId
     */
    public String getRetryTaskUniqueId(String callbackTaskUniqueId) {
        return callbackTaskUniqueId.substring(callbackTaskUniqueId.lastIndexOf(StrUtil.UNDERLINE) + 1);
    }

}
