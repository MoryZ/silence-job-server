package com.old.silence.job.server.job.task.support.executor.job;

import org.springframework.beans.factory.InitializingBean;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.job.task.support.JobExecutor;


public abstract class AbstractJobExecutor implements JobExecutor, InitializingBean {

    @Override
    public void execute(JobExecutorContext context) {
        if (CollectionUtils.isEmpty(context.getTaskList())) {
            SilenceJobLog.LOCAL.warn("待执行的任务列表为空. taskBatchId:[{}]", context.getTaskBatchId());
            return;
        }
        doExecute(context);
    }

    protected abstract void doExecute(JobExecutorContext context);

    @Override
    public void afterPropertiesSet() throws Exception {
        JobExecutorFactory.registerJobExecutor(getTaskInstanceType(), this);
    }
}
