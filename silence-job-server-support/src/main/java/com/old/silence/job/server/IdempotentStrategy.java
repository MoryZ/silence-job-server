package com.old.silence.job.server.common;

/**
 * 幂等策略
 *
 */
public interface IdempotentStrategy<T> {

    boolean set(T key);

    boolean isExist(T key);

    boolean clear(T key);

}
