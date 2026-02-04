package com.old.silence.job.server.common.rpc.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.CharsetUtil;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.common.rpc.RpcContext;
import com.old.silence.job.log.SilenceJobLog;


/**
 * netty 客户端处理器
 *
 */

public class NettyHttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    public NettyHttpClientHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {

        String content = msg.content().toString(CharsetUtil.UTF_8);
        HttpHeaders headers = msg.headers();

        SilenceJobLog.LOCAL.debug("Receive server data content:[{}], headers:[{}]", content, headers);
        SilenceJobRpcResult silenceJobRpcResult = JSON.parseObject(content, SilenceJobRpcResult.class);
        RpcContext.invoke(silenceJobRpcResult.getReqId(), silenceJobRpcResult, false);

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        SilenceJobLog.LOCAL.debug("channelRegistered");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        SilenceJobLog.LOCAL.debug("channelUnregistered");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        SilenceJobLog.LOCAL.debug("channelActive");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        SilenceJobLog.LOCAL.debug("channelInactive");
        NettyChannel.removeChannel(ctx.channel());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        SilenceJobLog.LOCAL.debug("channelReadComplete");
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
        SilenceJobLog.LOCAL.debug("channelWritabilityChanged");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SilenceJobLog.LOCAL.error("silence-job netty-http client exception", cause);
        super.exceptionCaught(ctx, cause);
        NettyChannel.removeChannel(ctx.channel());

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        NettyChannel.removeChannel(ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        SilenceJobLog.LOCAL.debug("userEventTriggered");
    }
}
