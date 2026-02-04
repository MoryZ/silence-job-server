package com.old.silence.job.server.job.task.support.block.job;

import org.springframework.beans.factory.InitializingBean;
import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.server.job.task.support.BlockStrategy;


public abstract class AbstracJobBlockStrategy implements BlockStrategy, InitializingBean {

    @Override
    public void block(BlockStrategyContext context) {
        doBlock(context);
    }

    protected abstract void doBlock(BlockStrategyContext context);


    protected abstract JobBlockStrategy blockStrategyEnum();

    @Override
    public void afterPropertiesSet() throws Exception {
        JobBlockStrategyFactory.registerBlockStrategy(blockStrategyEnum(), this);
    }
}
