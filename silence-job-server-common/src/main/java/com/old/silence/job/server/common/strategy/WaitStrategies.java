package com.old.silence.job.server.common.strategy;

import cn.hutool.core.lang.Assert;
import com.google.common.base.Preconditions;
import com.old.silence.job.common.enums.DelayLevelEnum;
import com.old.silence.job.common.exception.SilenceJobCommonException;
import com.old.silence.job.common.util.CronExpression;
import com.old.silence.job.server.common.WaitStrategy;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.exception.SilenceJobServerException;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 生成 {@link WaitStrategy} 实例.
 *
 */
public class WaitStrategies {

    private WaitStrategies() {
    }


    public static class WaitStrategyContext {

        /**
         * 间隔时长
         */
        private String triggerInterval;

        /**
         * 下次触发时间
         */
        private long nextTriggerAt;

        /**
         * 延迟等级
         * 仅在选择 DELAY_LEVEL时使用 {@link DelayLevelEnum}
         */
        private Integer delayLevel;


        public String getTriggerInterval() {
            return triggerInterval;
        }

        public void setTriggerInterval(String triggerInterval) {
            this.triggerInterval = triggerInterval;
        }

        public long getNextTriggerAt() {
            return nextTriggerAt;
        }

        public Integer getDelayLevel() {
            return delayLevel;
        }

        public void setDelayLevel(Integer delayLevel) {
            this.delayLevel = delayLevel;
        }

        public void setNextTriggerAt(long nextTriggerAt) {
            this.nextTriggerAt = nextTriggerAt;
        }

        public void setNextTriggerAt(Instant nextTriggerAt) {
            this.nextTriggerAt = DateUtils.toEpochMilli(nextTriggerAt);
        }
    }

    
    public enum WaitStrategyEnum {
        DELAY_LEVEL(1, delayLevelWait()),
        FIXED(2, fixedWait()),
        CRON(3, cronWait()),
        RANDOM(4, randomWait());

        private final int value;
        private final WaitStrategy waitStrategy;

        WaitStrategyEnum(int value, WaitStrategy waitStrategy) {
            this.value = value;
            this.waitStrategy = waitStrategy;
        }

        public int getValue() {
            return value;
        }

        public WaitStrategy getWaitStrategy() {
            return waitStrategy;
        }

        /**
         * 获取退避策略
         *
         * @param backOff 退避策略
         * @return 退避策略
         */
        public static WaitStrategy getWaitStrategy(int backOff) {
            return getWaitStrategyEnum(backOff).getWaitStrategy();

        }

        /**
         * 获取等待策略类型枚举对象
         *
         * @param type 等待策略类型
         * @return 等待策略类型枚举对象
         */
        public static WaitStrategyEnum getWaitStrategyEnum(int type) {

            for (WaitStrategyEnum waitStrategyEnum : WaitStrategyEnum.values()) {
                if (waitStrategyEnum.getValue() == type ) {
                    return waitStrategyEnum;
                }
            }

            // 兜底为默认等级策略
            throw new SilenceJobCommonException("等待策略类型不存在. [{}]", type);
        }

    }

    /**
     * 延迟等级等待策略
     *
     * @return {@link DelayLevelWaitStrategy} 延迟等级等待策略
     */
    public static WaitStrategy delayLevelWait() {
        return new DelayLevelWaitStrategy();
    }

    /**
     * 固定定时间等待策略
     *
     * @return {@link FixedWaitStrategy} 固定定时间等待策略
     */
    public static WaitStrategy fixedWait() {
        return new FixedWaitStrategy();
    }

    /**
     * cron等待策略
     *
     * @return {@link CronWaitStrategy} cron等待策略
     */
    public static WaitStrategy cronWait() {
        return new CronWaitStrategy();
    }

    /**
     * 随机等待策略
     *
     * @return {@link RandomWaitStrategy} 随机等待策略
     */
    public static WaitStrategy randomWait(long minimumTime, TimeUnit minimumTimeUnit, long maximumTime, TimeUnit maximumTimeUnit) {
        return new RandomWaitStrategy(minimumTimeUnit.toMillis(minimumTime), maximumTimeUnit.toMillis(maximumTime));
    }

    /**
     * 随机等待策略
     *
     * @return {@link RandomWaitStrategy} 随机等待策略
     */
    public static WaitStrategy randomWait() {
        return new RandomWaitStrategy();
    }

    /**
     * 延迟等级等待策略
     */
    private static final class DelayLevelWaitStrategy implements WaitStrategy {

        @Override
        public Long computeTriggerTime(WaitStrategyContext context) {
            DelayLevelEnum levelEnum = DelayLevelEnum.getDelayLevelByLevel(context.getDelayLevel());
            Duration of = Duration.of(levelEnum.getTime(), levelEnum.getUnit());
            return context.getNextTriggerAt() + of.toMillis();
        }
    }

    /**
     * 固定定时间等待策略
     */
    private static final class FixedWaitStrategy implements WaitStrategy {

        @Override
        public Long computeTriggerTime(WaitStrategyContext retryContext) {
            return retryContext.getNextTriggerAt() + DateUtils.toEpochMilli(Integer.parseInt(retryContext.getTriggerInterval()));
        }
    }

    /**
     * Cron等待策略
     */
    private static final class CronWaitStrategy implements WaitStrategy {

        @Override
        public Long computeTriggerTime(WaitStrategyContext context) {

            try {
                Date nextValidTime = new CronExpression(context.getTriggerInterval()).getNextValidTimeAfter(new Date(context.getNextTriggerAt()));
                Assert.notNull(nextValidTime, () -> new SilenceJobServerException("表达式错误:{}", context.getTriggerInterval()));
                return DateUtils.toEpochMilli(nextValidTime);
            } catch (ParseException e) {
                throw new SilenceJobServerException("解析CRON表达式异常 [{}]", context.getTriggerInterval(), e);
            }

        }
    }

    /**
     * 随机等待策略
     */
    private static final class RandomWaitStrategy implements WaitStrategy {

        private static final Random RANDOM = new Random();
        private final long minimum;
        private long maximum;

        public RandomWaitStrategy(long minimum, long maximum) {
            Preconditions.checkArgument(minimum >= 0, "minimum must be >= 0 but is %d", minimum);
            Preconditions.checkArgument(maximum > minimum, "maximum must be > minimum but maximum is %d and minimum is", maximum, minimum);

            this.minimum = minimum;
            this.maximum = maximum;
        }

        public RandomWaitStrategy() {
            this.minimum = 0;
        }

        @Override
        public Long computeTriggerTime(WaitStrategyContext retryContext) {

            if (Objects.nonNull(retryContext)) {
                if (maximum == 0) {
                    maximum = Long.parseLong(retryContext.getTriggerInterval());
                }
            }

            Preconditions.checkArgument(maximum > minimum, "maximum must be > minimum but maximum is %d and minimum is", maximum, minimum);

            long t = Math.abs(RANDOM.nextLong()) % (maximum - minimum);
            return (t + minimum + DateUtils.toNowMilli());
        }
    }
}
