package com.old.silence.job.server.task.common.base;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 阻塞策略工厂（泛型化，策略模式 + 工厂模式）
 * 统一 Job 和 Retry 模块的阻塞策略管理
 *
 * @param <C> 上下文类型
 * @param <E> 枚举类型
 * @author mory
 */
public abstract class BlockStrategyFactory<C extends BlockStrategyContext<E>, E extends Enum<E>> {

    /**
     * 策略注册表
     */
    private final ConcurrentHashMap<E, BlockStrategy<C>> strategies = new ConcurrentHashMap<>();

    /**
     * 注册阻塞策略
     *
     * @param strategyEnum 策略枚举
     * @param strategy     策略实例
     */
    public void registerBlockStrategy(E strategyEnum, BlockStrategy<C> strategy) {
        strategies.put(strategyEnum, strategy);
    }

    /**
     * 根据枚举获取策略
     *
     * @param strategyEnum 策略枚举
     * @return 策略实例
     */
    public BlockStrategy<C> getStrategy(E strategyEnum) {
        return strategies.get(strategyEnum);
    }

    /**
     * 执行阻塞策略
     *
     * @param context 策略上下文
     */
    public void executeBlock(C context) {
        E strategyEnum = context.getBlockStrategy();
        BlockStrategy<C> strategy = getStrategy(strategyEnum);
        if (strategy != null) {
            strategy.block(context);
        } else {
            throw new IllegalArgumentException("Unknown block strategy: " + strategyEnum);
        }
    }
}
