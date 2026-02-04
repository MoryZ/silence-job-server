package com.old.silence.job.server.task.common.timer;

import lombok.Builder;
import lombok.Getter;

/**
 * 时间轮配置
 *
 * @author mory
 */
@Getter
@Builder
public class TimerWheelConfig {

    /**
     * Tick duration (ms)
     */
    @Builder.Default
    private int tickDuration = 100;

    /**
     * 时间轮的槽数
     */
    @Builder.Default
    private int ticksPerWheel = 512;

    /**
     * 线程池核心线程数
     */
    @Builder.Default
    private int corePoolSize = 16;

    /**
     * 线程池最大线程数
     */
    @Builder.Default
    private int maximumPoolSize = 16;

    /**
     * 线程名前缀
     */
    @Builder.Default
    private String threadNamePrefix = "task-timer-wheel-";

    /**
     * 幂等性缓存并发级别
     */
    @Builder.Default
    private int idempotentConcurrencyLevel = 16;

    /**
     * 幂等性缓存过期时间（秒）
     */
    @Builder.Default
    private long idempotentExpireSeconds = 20;
}
