package com.old.silence.job.server.job.task.support.block.job;

import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.server.job.task.support.BlockStrategy;

import java.util.concurrent.ConcurrentHashMap;


public final class JobBlockStrategyFactory {
    private static final ConcurrentHashMap<JobBlockStrategy, BlockStrategy> CACHE = new ConcurrentHashMap<>();

    private JobBlockStrategyFactory() {
    }

    static void registerBlockStrategy(JobBlockStrategy jobBlockStrategy, BlockStrategy blockStrategy) {
        CACHE.put(jobBlockStrategy, blockStrategy);
    }

    public static BlockStrategy getBlockStrategy(JobBlockStrategy blockStrategy) {
        return CACHE.get(blockStrategy);
    }

}
