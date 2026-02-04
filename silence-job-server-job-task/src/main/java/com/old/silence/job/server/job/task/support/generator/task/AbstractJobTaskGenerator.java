package com.old.silence.job.server.job.task.support.generator.task;

import cn.hutool.core.lang.Assert;
import org.springframework.beans.factory.InitializingBean;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;

import java.util.List;


public abstract class AbstractJobTaskGenerator implements JobTaskGenerator, InitializingBean {

    protected final JobTaskDao jobTaskDao;

    protected AbstractJobTaskGenerator(JobTaskDao jobTaskDao) {
        this.jobTaskDao = jobTaskDao;
    }

    @Override
    public List<JobTask> generate(JobTaskGenerateContext context) {
        return doGenerate(context);
    }

    protected abstract List<JobTask> doGenerate(JobTaskGenerateContext context);

    @Override
    public void afterPropertiesSet() throws Exception {
        JobTaskGeneratorFactory.registerTaskInstance(getTaskInstanceType(), this);
    }

    protected void batchSaveJobTasks(List<JobTask> jobTasks) {
        Assert.isTrue(jobTasks.size() == jobTaskDao.insertBatch(jobTasks), () -> new SilenceJobServerException("新增任务实例失败"));
    }
}
