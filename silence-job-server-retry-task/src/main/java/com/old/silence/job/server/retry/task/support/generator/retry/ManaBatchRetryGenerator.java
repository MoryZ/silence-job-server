package com.old.silence.job.server.retry.task.support.generator.retry;

import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.RetryStatus;
import com.old.silence.job.common.enums.TaskGeneratorSceneEnum;
import com.old.silence.job.server.common.config.SystemProperties;
import com.old.silence.job.server.domain.service.AccessTemplate;

import java.util.Optional;

/**
 * 控制台手动批量新增
 *
 */
@Component
public class ManaBatchRetryGenerator extends AbstractGenerator {
    protected ManaBatchRetryGenerator(AccessTemplate accessTemplate, SystemProperties systemProperties) {
        super(accessTemplate, systemProperties);
    }

    @Override
    public boolean supports(int scene) {
        return TaskGeneratorSceneEnum.MANA_BATCH.getScene() == scene;
    }

    @Override
    protected RetryStatus initStatus(TaskContext taskContext) {
        return Optional.ofNullable(taskContext.getInitStatus()).orElse(RetryStatus.RUNNING);
    }
}
