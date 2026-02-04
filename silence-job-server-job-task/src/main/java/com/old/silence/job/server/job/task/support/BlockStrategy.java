package com.old.silence.job.server.job.task.support;

import com.old.silence.job.server.job.task.support.block.job.BlockStrategyContext;


public interface BlockStrategy {

    /**
     * 阻塞策略
     *
     * @param context 策略上下文
     */
    void block(BlockStrategyContext context);
}
