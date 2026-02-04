package com.old.silence.job.server.job.task.support.block.workflow;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;
import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.server.job.task.support.BlockStrategy;
import com.old.silence.job.server.job.task.support.block.job.BlockStrategyContext;


public abstract class AbstractWorkflowBlockStrategy implements BlockStrategy, InitializingBean {

    @Override
    @Transactional
    public void block(BlockStrategyContext context) {
        WorkflowBlockStrategyContext workflowBlockStrategyContext = (WorkflowBlockStrategyContext) context;

        doBlock(workflowBlockStrategyContext);
    }

    protected abstract void doBlock(WorkflowBlockStrategyContext workflowBlockStrategyContext);

    protected abstract JobBlockStrategy blockStrategyEnum();

    @Override
    public void afterPropertiesSet() throws Exception {
        WorkflowBlockStrategyFactory.registerBlockStrategy(blockStrategyEnum(), this);
    }
}
