package com.old.silence.job.server.retry.task.support.block;

import com.old.silence.job.common.enums.RetryBlockStrategy;
import com.old.silence.job.server.retry.task.support.BlockStrategy;

import java.util.concurrent.ConcurrentHashMap;


public final class RetryBlockStrategyFactory {
    private static final ConcurrentHashMap<RetryBlockStrategy, BlockStrategy> CACHE = new ConcurrentHashMap<>();

    private RetryBlockStrategyFactory() {
    }

    static void registerBlockStrategy(RetryBlockStrategy jobBlockStrategyEnum, BlockStrategy blockStrategy) {
        CACHE.put(jobBlockStrategyEnum, blockStrategy);
    }

    public static BlockStrategy getBlockStrategy(RetryBlockStrategy blockStrategy) {
        return CACHE.get(blockStrategy);
    }

}
