package com.old.silence.job.server.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.old.silence.job.server.SilenceJobCenterApplication;
import com.old.silence.job.server.event.ServerStartupFailedEvent;

/**
 * @author moryzang
 */
@Component
public class PlatformStartupFailureListener {

    @EventListener
    public void handleStartupFailure(ServerStartupFailedEvent event) {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) event.getSource();
        SpringApplication.exit(context, () -> 1); // 退出当前上下文
        SpringApplication.run(SilenceJobCenterApplication.class); // 重启应用
    }
}