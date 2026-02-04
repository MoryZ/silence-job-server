package com.old.silence.job.server.common.rpc.server;

import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ServerCalls;
import io.grpc.util.MutableHandlerRegistry;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.old.silence.job.common.constant.GrpcServerConstants;
import com.old.silence.job.common.enums.RpcType;
import com.old.silence.job.common.grpc.auto.GrpcResult;
import com.old.silence.job.common.grpc.auto.GrpcSilenceJobRequest;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;
import com.old.silence.job.server.common.config.SystemProperties;
import com.old.silence.job.server.common.config.SystemProperties.RpcServerProperties;
import com.old.silence.job.server.common.config.SystemProperties.ThreadPoolConfig;
import com.old.silence.job.server.exception.SilenceJobServerException;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * netty server
 *
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GrpcServer implements Lifecycle {

    private final SystemProperties systemProperties;
    private volatile boolean started = false;
    private Server server;

    public GrpcServer(SystemProperties systemProperties) {
        this.systemProperties = systemProperties;
    }

    @Override
    public void start() {
        // 防止重复启动
        if (started) {
            return;
        }

        if (RpcType.GRPC != systemProperties.getRpcType()) {
            return;
        }

        RpcServerProperties grpc = systemProperties.getServerRpc();

        MutableHandlerRegistry handlerRegistry = new MutableHandlerRegistry();
        addServices(handlerRegistry, new GrpcInterceptor());
        NettyServerBuilder builder = NettyServerBuilder.forPort(systemProperties.getServerPort())
            .executor(createGrpcExecutor(grpc.getDispatcherTp()));

        Duration keepAliveTime = grpc.getKeepAliveTime();
        Duration keepAliveTimeOut = grpc.getKeepAliveTimeout();
        Duration permitKeepAliveTime = grpc.getPermitKeepAliveTime();

        server = builder.maxInboundMessageSize(grpc.getMaxInboundMessageSize()).fallbackHandlerRegistry(handlerRegistry)
            .compressorRegistry(CompressorRegistry.getDefaultInstance())
            .decompressorRegistry(DecompressorRegistry.getDefaultInstance())
            .keepAliveTime(keepAliveTime.toMillis(), TimeUnit.MILLISECONDS)
            .keepAliveTimeout(keepAliveTimeOut.toMillis(), TimeUnit.MILLISECONDS)
            .permitKeepAliveTime(permitKeepAliveTime.toMillis(), TimeUnit.MILLISECONDS)
            .build();
        try {
            server.start();
            this.started = true;
            SilenceJobLog.LOCAL.info("------> grpcServer silence-job remoting server start success, grpc = {}, port = {}",
                GrpcServer.class.getName(), systemProperties.getServerPort());
        } catch (IOException e) {
            SilenceJobLog.LOCAL.error("--------> grpcServer silence-job remoting server error.", e);
            started = false;
            throw new SilenceJobServerException(" grpcServer silence-job server start error");
        }
    }

    @Override
    public void close() {
        if (server != null) {
            server.shutdownNow();
        }
    }

    private void addServices(MutableHandlerRegistry handlerRegistry, ServerInterceptor... serverInterceptor) {

        // 创建服务UNARY类型定义
        ServerServiceDefinition serviceDefinition = createUnaryServiceDefinition(
            GrpcServerConstants.UNARY_SERVICE_NAME, GrpcServerConstants.UNARY_METHOD_NAME,
            new UnaryRequestHandler());
        handlerRegistry.addService(serviceDefinition);
        // unary common call register.

        handlerRegistry.addService(ServerInterceptors.intercept(serviceDefinition, serverInterceptor));
    }

    public static ServerServiceDefinition createUnaryServiceDefinition(
        String serviceName,
        String methodName,
        ServerCalls.UnaryMethod<GrpcSilenceJobRequest, GrpcResult> unaryMethod) {

        MethodDescriptor<GrpcSilenceJobRequest, GrpcResult> methodDescriptor =
            MethodDescriptor.<GrpcSilenceJobRequest, GrpcResult>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(MethodDescriptor.generateFullMethodName(serviceName, methodName))
                .setRequestMarshaller(ProtoUtils.marshaller(GrpcSilenceJobRequest.getDefaultInstance()))
                .setResponseMarshaller(ProtoUtils.marshaller(GrpcResult.getDefaultInstance()))
                .build();

        return ServerServiceDefinition.builder(serviceName)
            .addMethod(methodDescriptor, ServerCalls.asyncUnaryCall(unaryMethod))
            .build();
    }

    private ThreadPoolExecutor createGrpcExecutor(ThreadPoolConfig threadPool) {
        ThreadPoolExecutor grpcExecutor = new ThreadPoolExecutor(threadPool.getCorePoolSize(),
            threadPool.getMaximumPoolSize(), threadPool.getKeepAliveTime(), TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(threadPool.getQueueCapacity()),
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("silence-job-grpc-server-executor-%d")
                .build());
        grpcExecutor.allowCoreThreadTimeOut(true);
        return grpcExecutor;
    }

    public SystemProperties getSystemProperties() {
        return systemProperties;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
