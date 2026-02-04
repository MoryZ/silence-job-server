package com.old.silence.job.server.retry.task.support.generator.retry;


/**
 * 任务生成器
 *
 */
public interface TaskGenerator {

    /**
     * 获取匹配的模式
     *
     * @param scene 1. 客户端上报 2.控制台新增单个任务 3.控制台批量新增任务
     * @return 符合条件的生成器
     */
    boolean supports(int scene);

    /**
     * 任务生成器
     *
     * @param taskContext 任务列表
     */
    void taskGenerator(TaskContext taskContext);
}
