package com.old.silence.job.server.common.dto;



/**
 * 服务端节点扩展参数
 *
 */

public class ServerNodeExtAttrs {

    /**
     * web容器port
     */
    private Integer webPort;

    public Integer getWebPort() {
        return webPort;
    }

    public void setWebPort(Integer webPort) {
        this.webPort = webPort;
    }
}
