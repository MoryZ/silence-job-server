package com.old.silence.job.server.event;

import org.springframework.context.ApplicationEvent;

/**
 * WebSocket 发送事件
 */
public class WsSendEvent extends ApplicationEvent {

    /**
     * 会话id
     */
    private String sid;

    /**
     * 发送的消息
     */
    private String message;

    public WsSendEvent(Object source) {
        super(source);
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
