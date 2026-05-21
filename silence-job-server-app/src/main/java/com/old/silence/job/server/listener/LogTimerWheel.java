package com.old.silence.job.server.listener;

import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.TimerTask;
import com.old.silence.job.server.task.common.timer.AbstractTimerWheel;
import com.old.silence.job.server.task.common.timer.TimerWheelConfig;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 日志查询时间轮
 */
public class LogTimerWheel extends AbstractTimerWheel {

    private static final LogTimerWheel INSTANCE;

    static {
        TimerWheelConfig config = TimerWheelConfig.builder()
                .tickDuration(100)
                .ticksPerWheel(512)
                .corePoolSize(8)
                .maximumPoolSize(16)
                .threadNamePrefix("log-timer-wheel-")
                .idempotentConcurrencyLevel(4)
                .idempotentExpireSeconds(30)
                .build();
        INSTANCE = new LogTimerWheel(config);
        SilenceJobLog.LOCAL.info("LogTimerWheel initialized");
    }

    private LogTimerWheel(TimerWheelConfig config) {
        super(config);
    }

    /**
     * 注册日志查询任务
     *
     * @param task  任务
     * @param delay 延迟时间
     */
    public static synchronized void registerWithJobLog(Supplier<TimerTask<String>> task, Duration delay) {
        INSTANCE.register(task, delay);
    }

    public static boolean checkExisted(String idempotentKey) {
        return INSTANCE.isExisted(idempotentKey);
    }

    public static void removeCache(String idempotentKey) {
        INSTANCE.clearCache(idempotentKey);
    }
}
