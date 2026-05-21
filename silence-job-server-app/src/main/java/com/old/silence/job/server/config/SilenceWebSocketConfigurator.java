package com.old.silence.job.server.config;

import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.context.annotation.Configuration;

/**
 * WebSocket 跨域配置
 */
@Configuration
public class SilenceWebSocketConfigurator extends ServerEndpointConfig.Configurator {

    @Override
    public boolean checkOrigin(String originHeaderValue) {
        // 允许所有来源跨域访问
        return true;
    }

    @Override
    public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
        return super.getEndpointInstance(clazz);
    }
}
