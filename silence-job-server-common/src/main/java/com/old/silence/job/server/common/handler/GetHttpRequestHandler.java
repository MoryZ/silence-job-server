package com.old.silence.job.server.common.handler;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import io.netty.handler.codec.http.HttpHeaders;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.server.common.HttpRequestHandler;

/**
 * 处理GRT请求
 *
 */
public abstract class GetHttpRequestHandler implements HttpRequestHandler {

    @Override
    public SilenceJobRpcResult doHandler(String content, UrlBuilder builder, HttpHeaders headers) {
        UrlQuery query = builder.getQuery();

        return doHandler(content, query, headers);
    }

    public abstract SilenceJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers);
}
