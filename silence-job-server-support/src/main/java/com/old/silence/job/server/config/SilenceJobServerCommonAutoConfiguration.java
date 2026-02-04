package com.old.silence.job.server.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * web访问模块
 *
 */
@Configuration
@ComponentScan("com.old.silence.job.server.common.*")
public class SilenceJobServerCommonAutoConfiguration {

    @Bean
    public TaskScheduler scheduledExecutorService() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("silence-job-scheduled-thread-");
        return scheduler;
    }

    @Bean
    public TaskScheduler alarmExecutorService() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("silence-job-alarm-thread-");
        return scheduler;
    }
}
