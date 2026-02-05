package com.old.silence.job.server.retry.task.support.generator.retry;

import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.RetryStatus;
import com.old.silence.job.common.enums.TaskGeneratorSceneEnum;
import com.old.silence.job.server.common.config.SystemProperties;
import com.old.silence.job.server.infrastructure.persistence.dao.GroupConfigDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetrySceneConfigDao;

/**
 * 客户端上报任务生成器
 *
 */
@Component
public class ClientReportRetryGenerator extends AbstractGenerator {
    protected ClientReportRetryGenerator(RetryDao retryDao, RetrySceneConfigDao retrySceneConfigDao,
                                       GroupConfigDao groupConfigDao, SystemProperties systemProperties) {
        super(retryDao, retrySceneConfigDao, groupConfigDao, systemProperties);
    }

    @Override
    public boolean supports(int scene) {
        return TaskGeneratorSceneEnum.CLIENT_REPORT.getScene() == scene;
    }

    @Override
    protected RetryStatus initStatus(TaskContext taskContext) {
        return RetryStatus.RUNNING;
    }
}
