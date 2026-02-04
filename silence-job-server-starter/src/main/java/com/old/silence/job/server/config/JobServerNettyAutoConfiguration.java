package com.old.silence.job.server.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.old.silence.job.server.common.rpc.server.GrpcServer;
import com.old.silence.job.server.common.rpc.server.NettyHttpServer;
import com.old.silence.job.server.event.ServerStartupFailedEvent;

import java.util.concurrent.TimeUnit;

/**
 * @author moryzang
 */
@Configuration
@ConditionalOnClass({NettyHttpServer.class, GrpcServer.class})
public class JobServerNettyAutoConfiguration {


    private static final Logger log = LoggerFactory.getLogger(JobServerNettyAutoConfiguration.class);

    @Bean
    public ApplicationRunner nettyStartupChecker(NettyHttpServer nettyHttpServer, GrpcServer grpcServer,
                                                 ServletWebServerFactory serverFactory,  ApplicationEventPublisher eventPublisher,
                                                 ConfigurableApplicationContext context) {
        return args -> {
            // 判定Grpc或者Netty服务端是否正常启动
            boolean started = nettyHttpServer.isStarted() || grpcServer.isStarted();
            // 最长自旋10秒，保证nettyHttpServer启动完成
            int waitCount = 0;
            while (!started && waitCount < 100) {
                log.info("--------> silence-job server is staring....");
                TimeUnit.MILLISECONDS.sleep(100);
                waitCount++;
                started = nettyHttpServer.isStarted() || grpcServer.isStarted();
            }

            if (!started) {
              /*  log.error("--------> silence-job server startup failure.");
                // Netty启动失败，停止Web服务和Spring Boot应用程序
                serverFactory.getWebServer().stop();

                SpringApplication.exit(SpringApplication.run(SilencePlatformApplication.class));*/
                serverFactory.getWebServer().stop();
                eventPublisher.publishEvent(new ServerStartupFailedEvent(context));
            }
        };
    }
}
