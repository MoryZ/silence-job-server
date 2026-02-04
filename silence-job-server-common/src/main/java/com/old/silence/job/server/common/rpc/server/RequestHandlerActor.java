package com.old.silence.job.server.common.rpc.server;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.StrUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.pekko.actor.AbstractActor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.HeadersEnum;
import com.old.silence.job.common.model.SilenceJobRequest;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.HttpRequestHandler;
import com.old.silence.job.server.common.cache.CacheToken;
import com.old.silence.job.server.common.dto.NettyHttpRequest;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.util.Collection;

/**
 * 处理netty客户端请求
 *
 */
@Component(ActorGenerator.REQUEST_HANDLER_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)

public class RequestHandlerActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(NettyHttpRequest.class, nettyHttpRequest -> {

            final String uri = nettyHttpRequest.getUri();
            if (StrUtil.isBlank(uri)) {
                SilenceJobLog.LOCAL.error("uri can not be null");
                return;
            }

            ChannelHandlerContext channelHandlerContext = nettyHttpRequest.getChannelHandlerContext();

            final boolean keepAlive = nettyHttpRequest.isKeepAlive();
            final HttpMethod method = nettyHttpRequest.getMethod();
            final String content = nettyHttpRequest.getContent();
            final HttpHeaders headers = nettyHttpRequest.getHeaders();

            SilenceJobRpcResult result = null;
            try {
                result = doProcess(uri, content, method, headers);
            } catch (Exception e) {
                SilenceJobLog.LOCAL.error("http request error. [{}]", nettyHttpRequest.getContent(), e);
                SilenceJobRequest retryRequest = JSON.parseObject(content, SilenceJobRequest.class);
                result = new SilenceJobRpcResult(500, e.getMessage(), null, retryRequest.getReqId());
            } finally {
                writeResponse(channelHandlerContext, keepAlive, JSON.toJSONString(result));
                getContext().stop(getSelf());
            }


        }).build();
    }

    private SilenceJobRpcResult doProcess(String uri, String content, HttpMethod method,
                             HttpHeaders headers) {
        String groupName = headers.get(HeadersEnum.GROUP_NAME.getKey());
        String namespace = headers.get(HeadersEnum.NAMESPACE.getKey());
        String token = headers.get(HeadersEnum.TOKEN.getKey());

        if (StrUtil.isBlank(token) || !CacheToken.get(groupName, namespace).equals(token)) {
            throw new SilenceJobServerException("Token authentication failed. [namespace:{} groupName:{} token:{}]", namespace, groupName, token);
        }


        UrlBuilder builder = UrlBuilder.ofHttp(uri);
        Collection<HttpRequestHandler> httpRequestHandlers = SilenceSpringContext.getContext()
                .getBeansOfType(HttpRequestHandler.class).values();
        for (HttpRequestHandler httpRequestHandler : httpRequestHandlers) {
            if (httpRequestHandler.supports(builder.getPathStr()) && method.name()
                    .equals(httpRequestHandler.method().name())) {
                return httpRequestHandler.doHandler(content, builder, headers);
            }
        }

        throw new SilenceJobServerException("No matching handler found. Path:[{}] method:[{}]", builder.getPathStr(), method.name());
    }

    /**
     * write response
     */
    private void writeResponse(ChannelHandlerContext ctx, boolean keepAlive, String responseJson) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.copiedBuffer(responseJson, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,
                HttpHeaderValues.APPLICATION_JSON);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.writeAndFlush(response);
    }

}
