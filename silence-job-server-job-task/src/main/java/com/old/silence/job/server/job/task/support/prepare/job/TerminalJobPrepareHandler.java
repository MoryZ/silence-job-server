package com.old.silence.job.server.job.task.support.prepare.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.server.job.task.dto.JobTaskPrepareDTO;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.job.task.support.generator.batch.JobTaskBatchGenerator;

import static com.old.silence.job.common.enums.JobTaskBatchStatus.COMPLETED;

/**
 * 处理处于已完成 {@link JobTaskBatchStatus ::COMPLETED} 状态的任务
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component

public class TerminalJobPrepareHandler extends AbstractJobPrepareHandler {


    private static final Logger log = LoggerFactory.getLogger(TerminalJobPrepareHandler.class);
    private final JobTaskBatchGenerator jobTaskBatchGenerator;

    public TerminalJobPrepareHandler(JobTaskBatchGenerator jobTaskBatchGenerator) {
        this.jobTaskBatchGenerator = jobTaskBatchGenerator;
    }

    @Override
    public boolean matches(JobTaskBatchStatus status) {
        return COMPLETED.contains(status);
    }

    @Override
    protected void doHandle(JobTaskPrepareDTO jobPrepareDTO) {
        log.debug("无处理中的数据. jobId:[{}]", jobPrepareDTO.getJobId());

        // 生成任务批次
        jobTaskBatchGenerator.generateJobTaskBatch(JobTaskConverter.INSTANCE.toJobTaskGeneratorContext(jobPrepareDTO));
    }
}
