package com.old.silence.job.server.common.rpc.client;

import com.github.rholder.retry.RetryListener;

import java.util.Map;


public interface SilenceJobRetryListener extends RetryListener {

    /**
     * 传递属性信息
     *
     * @return Map<String, Object>
     */
    Map<String, Object> properties();

}
