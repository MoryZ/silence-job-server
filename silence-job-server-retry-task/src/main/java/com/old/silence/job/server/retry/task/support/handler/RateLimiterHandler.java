package com.old.silence.job.server.retry.task.support.handler;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import com.google.common.util.concurrent.RateLimiter;
import com.old.silence.job.server.common.config.SystemProperties;

import java.util.concurrent.TimeUnit;


@Component
public class RateLimiterHandler implements InitializingBean {
    private final SystemProperties systemProperties;
    private RateLimiter rateLimiter;

    public RateLimiterHandler(SystemProperties systemProperties) {
        this.systemProperties = systemProperties;
    }

    public boolean tryAcquire(int permits) {
        return rateLimiter.tryAcquire(permits, 500L, TimeUnit.MILLISECONDS);
    }


    public void refreshRate( ) {
        int maxDispatchCapacity = systemProperties.getMaxDispatchCapacity();
        if (maxDispatchCapacity == rateLimiter.getRate()) {
            return;
        }
        rateLimiter.setRate(maxDispatchCapacity);
    }

    public void refreshRate(int maxDispatchCapacity ) {
        if (maxDispatchCapacity == rateLimiter.getRate()) {
            return;
        }
        rateLimiter.setRate(maxDispatchCapacity);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        rateLimiter  = RateLimiter.create(systemProperties.getMaxDispatchCapacity());
    }

}
