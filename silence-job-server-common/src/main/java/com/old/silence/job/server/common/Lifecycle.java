package com.old.silence.job.server.common;

/**
 * 组件生命周期
 *
 */
public interface Lifecycle {

    /**
     * 启动组件
     */
    void start();

    /**
     * 关闭组件
     */
    void close();

}
