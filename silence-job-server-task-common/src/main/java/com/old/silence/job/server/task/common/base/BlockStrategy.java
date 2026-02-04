package com.old.silence.job.server.task.common.base;

/**
 * 阻塞策略接口（泛型化）
 * 统一 Job 和 Retry 模块的阻塞策略
 *
 * @param <C> 上下文类型
 * @author mory
 */
public interface BlockStrategy<C extends BlockStrategyContext<?>> {

    /**
     * 执行阻塞策略
     *
     * @param context 策略上下文
     */
    void block(C context);
}
