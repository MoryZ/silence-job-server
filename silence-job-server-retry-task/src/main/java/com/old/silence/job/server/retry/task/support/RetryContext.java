package com.old.silence.job.server.retry.task.support;

import com.old.silence.job.server.common.WaitStrategy;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;
import com.old.silence.job.server.domain.model.Retry;
import com.old.silence.job.server.domain.model.RetrySceneConfig;

import java.util.Set;


public interface RetryContext<V> {

    /**
     * 获取重试数据
     *
     */
    Retry getRetryTask();

    /**
     * 回调接果
     *
     * @param v 回调的数据
     */
    void setCallResult(V v);

    /**
     * 获取回调接果
     *
     * @return 回调的数据
     */
    V getCallResult();

    /**
     * 调用客户端发生异常信息
     *
     * @param e 异常
     */
    void setException(Exception e);

    /**
     * 获取客户端发送异常信息
     *
     */
    Exception getException();

    /**
     * 是否发生异常
     *
     */
    boolean hasException();

    /**
     * 等待策略
     *
     * @param waitStrategy {@link WaitStrategy} 等待策略
     */
    void setWaitStrategy(WaitStrategy waitStrategy);

    /**
     * 获取等待测试
     *
     * @return {@link WaitStrategy} 等待策略
     */
    WaitStrategy getWaitStrategy();

    /**
     * 获取分配的节点
     *
     * @return {@link RegisterNodeInfo} 注册的节点信息
     */
    RegisterNodeInfo getServerNode();

    /**
     * 获取场景黑名单
     *
     * @return 场景集合
     */
    Set<String> getSceneBlacklist();

    /**
     * 路由策略
     *
     * @return 路由策略
     */
    RetrySceneConfig sceneConfig();
}
