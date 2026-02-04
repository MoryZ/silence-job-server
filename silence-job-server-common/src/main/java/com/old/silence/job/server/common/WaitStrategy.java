package com.old.silence.job.server.common;

import com.old.silence.job.server.common.strategy.WaitStrategies;

/**
 * 等待策略（退避策略）
 *
 */
public interface WaitStrategy {

    /**
     * 计算下次重试触发时间
     *
     * @param waitStrategyContext {@link WaitStrategies.WaitStrategyContext} 重试上下文
     * @return 下次触发时间
     */
    Long computeTriggerTime(WaitStrategies.WaitStrategyContext waitStrategyContext);

}
