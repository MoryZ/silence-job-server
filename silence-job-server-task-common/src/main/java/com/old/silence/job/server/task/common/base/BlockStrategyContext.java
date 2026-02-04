package com.old.silence.job.server.task.common.base;

/**
 * 阻塞策略上下文接口（泛型化）
 * 统一 Job 和 Retry 模块的阻塞策略上下文
 *
 * @param <E> 枚举类型（JobBlockStrategy 或 RetryBlockStrategy）
 * @author mory
 */
public interface BlockStrategyContext<E extends Enum<E>> {

    /**
     * 获取阻塞策略枚举
     *
     * @return 阻塞策略枚举
     */
    E getBlockStrategy();
}
