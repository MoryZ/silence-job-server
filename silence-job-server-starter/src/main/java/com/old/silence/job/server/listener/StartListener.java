package com.old.silence.job.server.listener;

import org.jetbrains.annotations.NotNull;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.util.SilenceJobVersion;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;

import java.util.List;

/**
 * 系统启动监听器
 *
 */
@Component

public class StartListener implements ApplicationListener<ContextRefreshedEvent> {
    private final List<Lifecycle> lifecycleList;
    private volatile boolean isStarted = false;

    public StartListener(List<Lifecycle> lifecycleList) {
        this.lifecycleList = lifecycleList;
    }

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        if (isStarted) {
            SilenceJobLog.LOCAL.info("silence-job server already started v{}", SilenceJobVersion.getVersion());
            return;
        }

        System.out.println(MessageFormatter.format(SystemConstants.LOGO, SilenceJobVersion.getVersion()).getMessage());
        SilenceJobLog.LOCAL.info("silence-job server is preparing to start... v{}", SilenceJobVersion.getVersion());
        lifecycleList.forEach(Lifecycle::start);
        SilenceJobLog.LOCAL.info("silence-job server started successfully v{}", SilenceJobVersion.getVersion());
        isStarted = true;
    }
}
