package com.old.silence.job.server.common.rpc.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;
import org.apache.pekko.actor.ActorRef;
import com.old.silence.job.server.common.dto.NettyHttpRequest;
import com.old.silence.job.server.common.pekko.ActorGenerator;


public class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public NettyHttpServerHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
            throws Exception {

        NettyHttpRequest nettyHttpRequest = new NettyHttpRequest();
        nettyHttpRequest.setKeepAlive(HttpUtil.isKeepAlive(fullHttpRequest));
        nettyHttpRequest.setUri(fullHttpRequest.uri());
        nettyHttpRequest.setChannelHandlerContext(channelHandlerContext);
        nettyHttpRequest.setMethod(fullHttpRequest.method());
        nettyHttpRequest.setHeaders(fullHttpRequest.headers());
        nettyHttpRequest.setContent(fullHttpRequest.content().toString(CharsetUtil.UTF_8));

        ActorRef actorRef = ActorGenerator.requestHandlerActor();
        actorRef.tell(nettyHttpRequest, actorRef);
    }


}
