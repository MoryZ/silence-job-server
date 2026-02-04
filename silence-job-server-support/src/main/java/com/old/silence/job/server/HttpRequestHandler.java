package com.old.silence.job.server.common;

import cn.hutool.core.net.url.UrlBuilder;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import com.old.silence.job.common.model.SilenceJobRpcResult;

/**
 * 处理http请求
 *
 */
public interface HttpRequestHandler {

    boolean supports(String path);

    HttpMethod method();

    SilenceJobRpcResult doHandler(String content, UrlBuilder urlBuilder, HttpHeaders headers);

}
