package com.old.silence.job.server.retry.task.support.block;

import org.springframework.beans.factory.InitializingBean;
import com.old.silence.job.common.enums.RetryBlockStrategy;
import com.old.silence.job.server.retry.task.support.BlockStrategy;


public abstract class AbstracJobBlockStrategy implements BlockStrategy, InitializingBean {

    @Override
    public void block(final BlockStrategyContext context) {
        doBlock(context);
    }

    protected abstract void doBlock(final BlockStrategyContext context);

    protected abstract RetryBlockStrategy blockStrategyEnum();

    @Override
    public void afterPropertiesSet() throws Exception {
        RetryBlockStrategyFactory.registerBlockStrategy(blockStrategyEnum(), this);
    }
}
