package com.old.silence.job.server.common.rpc.client;

import cn.hutool.core.lang.Assert;
import com.github.rholder.retry.RetryListener;
import com.old.silence.core.context.CommonErrors;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.RpcType;
import com.old.silence.job.server.common.config.SystemProperties;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * 构建请求类型
 *
 */
public class RequestBuilder<T, R> {

    private Class<T> clintInterface;
    private RegisterNodeInfo nodeInfo;
    private boolean failRetry;
    private int retryTimes = 3;
    private int retryInterval = 1;
    private RetryListener retryListener = new SimpleRetryListener();
    private boolean failover;
    private int routeKey;
    private String allocKey;
    private Integer executorTimeout;

    public static <T, R> RequestBuilder<T, R> newBuilder() {
        return new RequestBuilder<>();
    }

    public RequestBuilder<T, R> client(Class<T> clintInterface) {
        this.clintInterface = clintInterface;
        return this;
    }

    public RequestBuilder<T, R> nodeInfo(RegisterNodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
        return this;
    }

    public RequestBuilder<T, R> executorTimeout(Integer executorTimeout) {
        if (Objects.isNull(executorTimeout)) {
            return this;
        }

        Assert.isTrue(executorTimeout > 0, () -> CommonErrors.INVALID_PARAMETER.createException("executorTimeout can not less 0"));
        this.executorTimeout = executorTimeout;
        return this;
    }

    public RequestBuilder<T, R> failRetry(boolean failRetry) {
        this.failRetry = failRetry;
        return this;
    }

    public RequestBuilder<T, R> retryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
        return this;
    }

    public RequestBuilder<T, R> retryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
        return this;
    }

    public RequestBuilder<T, R> retryListener(RetryListener retryListener) {
        this.retryListener = retryListener;
        return this;
    }

    public RequestBuilder<T, R> failover(boolean failover) {
        this.failover = failover;
        return this;
    }

    public RequestBuilder<T, R> routeKey(int routeKey) {
        this.routeKey = routeKey;
        return this;
    }

    public RequestBuilder<T, R> allocKey(String allocKey) {
        this.allocKey = allocKey;
        return this;
    }

    public T build() {
        if (Objects.isNull(clintInterface)) {
            throw CommonErrors.INVALID_PARAMETER.createException("clintInterface cannot be null");
        }

        Assert.notNull(nodeInfo, () -> CommonErrors.INVALID_PARAMETER.createException("nodeInfo cannot be null"));
        Assert.notBlank(nodeInfo.getNamespaceId(), () -> CommonErrors.INVALID_PARAMETER.createException("namespaceId cannot be null"));

        if (failover) {
            Assert.isTrue(routeKey > 0, () -> CommonErrors.INVALID_PARAMETER.createException("routeKey cannot be null"));
            Assert.notBlank(allocKey, () -> CommonErrors.INVALID_PARAMETER.createException("allocKey cannot be null"));
        }
        try {
            clintInterface = (Class<T>) Class.forName(clintInterface.getName());
        } catch (Exception e) {
            throw CommonErrors.INVALID_PARAMETER.createException("class not found exception to: [{}]", clintInterface.getName());
        }

        SystemProperties properties = SilenceSpringContext.getBean(SystemProperties.class);
        RpcType rpcType = properties.getRpcType();

        InvocationHandler invocationHandler;
        if (Objects.isNull(rpcType)) {
            invocationHandler = new RpcClientInvokeHandler(
                nodeInfo.getGroupName(), nodeInfo, failRetry, retryTimes, retryInterval,
                retryListener, routeKey, allocKey, failover, executorTimeout, nodeInfo.getNamespaceId());
        } else {
            invocationHandler = new GrpcClientInvokeHandler(
                nodeInfo.getGroupName(), nodeInfo, failRetry, retryTimes, retryInterval,
                retryListener, routeKey, allocKey, failover, executorTimeout, nodeInfo.getNamespaceId());
        }

        return (T) Proxy.newProxyInstance(clintInterface.getClassLoader(),
                new Class[]{clintInterface}, invocationHandler);
    }

}
