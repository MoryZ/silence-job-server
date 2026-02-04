package com.old.silence.job.server.task.common.base;

import org.springframework.beans.factory.InitializingBean;

/**
 * 阻塞策略抽象基类（泛型化，模板方法模式）
 * 统一 Job 和 Retry 模块的阻塞策略实现框架
 *
 * @param <C> 上下文类型
 * @param <E> 枚举类型
 * @param <F> 工厂类型
 * @author mory
 */
public abstract class AbstractBlockStrategy<C extends BlockStrategyContext<E>, E extends Enum<E>, F extends BlockStrategyFactory<C, E>>
        implements BlockStrategy<C>, InitializingBean {

    /**
     * 模板方法：执行阻塞策略
     *
     * @param context 策略上下文
     */
    @Override
    public void block(C context) {
        doBlock(context);
    }

    /**
     * 具体的阻塞逻辑由子类实现
     *
     * @param context 策略上下文
     */
    protected abstract void doBlock(C context);

    /**
     * 返回当前策略对应的枚举值
     *
     * @return 阻塞策略枚举
     */
    protected abstract E blockStrategyEnum();

    /**
     * 获取工厂实例（由子类提供）
     *
     * @return 工厂实例
     */
    protected abstract F getFactory();

    /**
     * 初始化时注册到工厂
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        getFactory().registerBlockStrategy(blockStrategyEnum(), this);
    }
}
