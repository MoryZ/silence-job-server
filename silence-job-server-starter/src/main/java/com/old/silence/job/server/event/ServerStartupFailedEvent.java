package com.old.silence.job.server.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author moryzang
 */
public class ServerStartupFailedEvent extends ApplicationEvent {
    public ServerStartupFailedEvent(Object source) {
        super(source);
    }
}