package com.old.silence.job.server.task.common.timer;

import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.TimerTask;
import com.old.silence.job.server.task.common.idempotent.TimerIdempotent;
import io.netty.util.HashedWheelTimer;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 时间轮抽象类（泛型化，统一 Job 和 Retry 逻辑）
 *
 * @author mory
 */
public abstract class AbstractTimerWheel {

    protected final HashedWheelTimer timer;
    protected final ThreadPoolExecutor executor;
    protected final TimerIdempotent idempotent;
    protected final TimerWheelConfig config;

    /**
     * 构造函数
     *
     * @param config 时间轮配置
     */
    protected AbstractTimerWheel(TimerWheelConfig config) {
        this.config = config;
        this.executor = new ThreadPoolExecutor(
                config.getCorePoolSize(),
                config.getMaximumPoolSize(),
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new CustomizableThreadFactory(config.getThreadNamePrefix())
        );
        this.idempotent = new TimerIdempotent(
                config.getIdempotentConcurrencyLevel(),
                config.getIdempotentExpireSeconds(),
                TimeUnit.SECONDS
        );
        this.timer = new HashedWheelTimer(
                new CustomizableThreadFactory(config.getThreadNamePrefix()),
                config.getTickDuration(),
                TimeUnit.MILLISECONDS,
                config.getTicksPerWheel(),
                true,
                -1,
                executor
        );
        this.timer.start();
        SilenceJobLog.LOCAL.info("TimerWheel 初始化完成. prefix:[{}] tick:[{}ms] pool:[{}]",
                config.getThreadNamePrefix(), config.getTickDuration(), config.getCorePoolSize());
    }

    /**
     * 注册任务到时间轮
     *
     * @param task  任务
     * @param delay 延迟时间
     */
    public synchronized void register(Supplier<TimerTask<String>> task, Duration delay) {
        TimerTask<String> timerTask = task.get();
        register(timerTask.idempotentKey(), timerTask, delay);
    }

    /**
     * 注册任务到时间轮
     *
     * @param idempotentKey 幂等 key
     * @param task          任务
     * @param delay         延迟时间
     */
    public synchronized void register(String idempotentKey, TimerTask<String> task, Duration delay) {
        register(idempotentKey, hashedWheelTimer -> {
            long delayMillis = Math.max(delay.toMillis(), 0);
            SilenceJobLog.LOCAL.debug("加入时间轮. delay:[{}ms] idempotentKey:[{}]", delayMillis, idempotentKey);
            timer.newTimeout(task, delayMillis, TimeUnit.MILLISECONDS);
        });
    }

    /**
     * 注册任务到时间轮（使用 Consumer）
     *
     * @param idempotentKey 幂等 key
     * @param consumer      Consumer
     */
    public synchronized void register(String idempotentKey, Consumer<HashedWheelTimer> consumer) {
        if (!isExisted(idempotentKey)) {
            try {
                consumer.accept(timer);
                idempotent.set(idempotentKey);
            } catch (Exception e) {
                SilenceJobLog.LOCAL.error("加入时间轮失败. uniqueId:[{}]", idempotentKey, e);
            }
        }
    }

    /**
     * 检查任务是否已存在
     *
     * @param idempotentKey 幂等 key
     * @return true - 存在，false - 不存在
     */
    public boolean isExisted(String idempotentKey) {
        return idempotent.isExist(idempotentKey);
    }

    /**
     * 清除缓存
     *
     * @param idempotentKey 幂等 key
     */
    public void clearCache(String idempotentKey) {
        idempotent.clear(idempotentKey);
    }

    /**
     * 停止时间轮
     */
    public void stop() {
        if (timer != null) {
            timer.stop();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }
}
