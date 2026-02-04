package com.old.silence.job.server.common.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;

import java.util.concurrent.TimeUnit;

/**
 * 初始化bootstrap
 *
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NettyHttpConnectClient implements Lifecycle {

    private static final NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
    private static final Bootstrap bootstrap = new Bootstrap();

    @Override
    public void start() {

        try {
            bootstrap.group(nioEventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS))
                                    .addLast(new HttpClientCodec())
                                    .addLast(new HttpObjectAggregator(5 * 1024 * 1024))
                                    .addLast(new NettyHttpClientHandler());
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
            NettyChannel.setBootstrap(bootstrap);
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("Client start exception", e);
        }
    }

    @Override
    public void close() {
    }

}
