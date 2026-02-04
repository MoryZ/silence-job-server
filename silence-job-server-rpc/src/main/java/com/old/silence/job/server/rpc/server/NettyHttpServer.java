package com.old.silence.job.server.common.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.RpcType;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;
import com.old.silence.job.server.common.config.SystemProperties;
import com.old.silence.job.server.exception.SilenceJobServerException;

/**
 * netty server
 *
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NettyHttpServer implements Runnable, Lifecycle {
    private final SystemProperties systemProperties;
    private Thread thread = null;
    private volatile boolean started = false;

    public NettyHttpServer(SystemProperties systemProperties) {
        this.systemProperties = systemProperties;
    }

    @Override
    public void run() {
        // 防止重复启动
        if (started) {
            return;
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // start server
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new HttpServerCodec())
                                    .addLast(new HttpObjectAggregator(5 * 1024 * 1024))
                                    .addLast(new NettyHttpServerHandler());
                        }
                    });

            // 在特定端口绑定并启动服务器 默认是17888
            ChannelFuture future = bootstrap.bind(systemProperties.getServerPort()).sync();

            SilenceJobLog.LOCAL.info("------> silence-job remoting server start success, nettype = {}, port = {}",
                    NettyHttpServer.class.getName(), systemProperties.getServerPort());

            started = true;
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            SilenceJobLog.LOCAL.info("--------> silence-job remoting server stop.");
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("--------> silence-job remoting server error.", e);
            started = false;
            throw new SilenceJobServerException("silence-job server start error");
        } finally {
            // 当服务器正常关闭时，关闭EventLoopGroups以释放资源。
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    @Override
    public void start() {

        if (RpcType.NETTY != systemProperties.getRpcType()) {
            return;
        }
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void close() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    public SystemProperties getSystemProperties() {
        return systemProperties;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }
}
