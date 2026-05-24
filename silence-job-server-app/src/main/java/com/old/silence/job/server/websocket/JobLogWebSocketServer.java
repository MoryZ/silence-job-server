package com.old.silence.job.server.websocket;

import com.old.silence.job.server.config.SilenceWebSocketConfigurator;
import com.old.silence.job.server.event.WsRequestEvent;
import com.old.silence.job.server.event.WsSendEvent;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 日志服务
 */
@Component
@ServerEndpoint(value = "/websocket", configurator = SilenceWebSocketConfigurator.class)
public class JobLogWebSocketServer {


    public static final String SID = "sid";
    public static final String SCENE = "scene";
    public static final String AUTH = "Snail-Job-Auth";

    /**
     * 缓存所有连接的 session
     */
    private static final Logger log = LoggerFactory.getLogger(JobLogWebSocketServer.class);
    public static final ConcurrentHashMap<String, Session> USER_SESSION = new ConcurrentHashMap<>();
    private static ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        JobLogWebSocketServer.applicationEventPublisher = publisher;
    }

    /**
     * 从 queryString 中解析参数
     */
    private Map<String, String> parseQueryParams(Session session) {
        Map<String, String> params = new HashMap<>();
        String queryString = session.getRequestURI().getQuery();
        if (queryString != null) {
            for (String param : queryString.split("&")) {
                String[] keyValue = param.split("=", 2);
                if (keyValue.length >= 2) {
                    String key = keyValue[0];
                    String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    params.put(key, value);
                }
            }
        }
        return params;
    }

    @OnOpen
    public void onOpen(Session session) {
        Map<String, String> params = parseQueryParams(session);
        String sid = params.get(SID);
        String scene = params.get(SCENE);

        USER_SESSION.put(sid, session);
        log.info("WebSocket connected. sid:[{}], scene:[{}]", sid, scene);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        Map<String, String> params = parseQueryParams(session);
        String sid = params.get(SID);
        String scene = params.get(SCENE);

        log.info("Received WebSocket message. sid:[{}], message:[{}]", sid, message);

        WsRequestEvent requestEvent = new WsRequestEvent(this);
        requestEvent.setSid(sid);
        requestEvent.setMessage(message);
        requestEvent.setSceneEnum(com.old.silence.job.server.common.enums.JobLogWebSocketSceneEnum.valueOfScene(scene));

        applicationEventPublisher.publishEvent(requestEvent);
    }

    @OnClose
    public void onClose(Session session) {
        Map<String, String> params = parseQueryParams(session);
        String sid = params.get(SID);

        log.info("WebSocket closed. sid:[{}]", sid);
        USER_SESSION.remove(sid);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        Map<String, String> params = parseQueryParams(session);
        String sid = params.get(SID);

        log.error("WebSocket error. sid:[{}]", sid, throwable);
    }

    /**
     * 发送消息给指定会话
     */
    public static void sendMessage(String sid, String message) {
        Session session = USER_SESSION.get(sid);
        if (Objects.isNull(session)) {
            log.warn("WebSocket session not found. sid:[{}]", sid);
            return;
        }

        if (session.isOpen()) {
            synchronized (session) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.error("Failed to send WebSocket message. sid:[{}]", sid, e);
                }
            }
        }
    }

    /**
     * 监听发送事件并推送消息
     */
    @org.springframework.context.event.EventListener
    public void handleWsSendEvent(WsSendEvent event) {
        sendMessage(event.getSid(), event.getMessage());
    }
}
