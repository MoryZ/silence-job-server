package com.old.silence.job.server.job.task.support.generator.task;

import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.domain.model.JobTask;

import java.util.List;


public interface JobTaskGenerator {

    JobTaskType getTaskInstanceType();

    List<JobTask> generate(JobTaskGenerateContext context);

}
