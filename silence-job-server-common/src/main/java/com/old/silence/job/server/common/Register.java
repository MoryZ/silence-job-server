package com.old.silence.job.server.common;

import com.old.silence.job.common.enums.NodeType;
import com.old.silence.job.server.common.register.RegisterContext;


public interface Register {

    /**
     * 节点类型  see: {@link NodeType}
     *
     * @param type 1. 客户端 2.服务端
     */
    boolean supports(NodeType type);

    /**
     * 执行注册
     *
     */
    boolean register(RegisterContext context);
}
