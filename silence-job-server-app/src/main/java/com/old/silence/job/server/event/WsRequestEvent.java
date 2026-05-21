package com.old.silence.job.server.event;

import com.old.silence.job.server.common.enums.JobLogWebSocketSceneEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * WebSocket 请求事件
 */
public class WsRequestEvent extends ApplicationEvent {

    /**
     * 会话id
     */
    private String sid;

    /**
     * 请求消息
     */
    private String message;

    /**
     * 场景类型
     */
    private JobLogWebSocketSceneEnum sceneEnum;

    public WsRequestEvent(Object source) {
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

    public JobLogWebSocketSceneEnum getSceneEnum() {
        return sceneEnum;
    }

    public void setSceneEnum(JobLogWebSocketSceneEnum sceneEnum) {
        this.sceneEnum = sceneEnum;
    }
}
