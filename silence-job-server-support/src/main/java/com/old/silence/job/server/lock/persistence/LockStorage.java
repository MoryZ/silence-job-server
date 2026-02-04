package com.old.silence.job.server.common.lock.persistence;

import com.old.silence.job.server.common.dto.LockConfig;

import java.time.Instant;


public interface LockStorage {

    /**
     * 创建锁记录
     *
     * @param lockConfig 锁配置
     */
    boolean createLock(LockConfig lockConfig);

    /**
     * 更新锁记录
     *
     * @param lockConfig 锁配置
     */
    boolean renewal(LockConfig lockConfig);

    /**
     * 删除锁记录释放锁
     *
     * @param lockName 锁名称
     */
    boolean releaseLockWithDelete(String lockName);

    /**
     * 更新锁定时长释放锁
     *
     * @param lockName    锁名称
     * @param lockAtLeast 最少锁定时长
     */
    boolean releaseLockWithUpdate(String lockName, Instant lockAtLeast);

}
