package com.old.silence.job.server.job.task.support.block.workflow;

import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.server.job.task.support.BlockStrategy;

import java.util.concurrent.ConcurrentHashMap;


public final class WorkflowBlockStrategyFactory {
    private static final ConcurrentHashMap<JobBlockStrategy, BlockStrategy> CACHE = new ConcurrentHashMap<>();

    private WorkflowBlockStrategyFactory() {
    }

    static void registerBlockStrategy(JobBlockStrategy jobBlockStrategy, BlockStrategy blockStrategy) {
        CACHE.put(jobBlockStrategy, blockStrategy);
    }

    public static BlockStrategy getBlockStrategy(JobBlockStrategy blockStrategy) {
        return CACHE.get(blockStrategy);
    }

}
