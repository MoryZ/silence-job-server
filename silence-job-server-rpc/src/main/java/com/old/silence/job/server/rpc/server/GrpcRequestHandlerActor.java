package com.old.silence.job.server.common.rpc.server;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.StrUtil;
import io.grpc.stub.StreamObserver;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.apache.pekko.actor.AbstractActor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.old.silence.core.context.CommonErrors;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.HeadersEnum;
import com.old.silence.job.common.grpc.auto.GrpcResult;
import com.old.silence.job.common.grpc.auto.GrpcSilenceJobRequest;
import com.old.silence.job.common.grpc.auto.Metadata;
import com.old.silence.job.common.model.SilenceJobRequest;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.HttpRequestHandler;
import com.old.silence.job.server.common.cache.CacheToken;
import com.old.silence.job.server.common.dto.GrpcRequest;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 处理netty客户端请求
 *
 */
@Component(ActorGenerator.GRPC_REQUEST_HANDLER_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GrpcRequestHandlerActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(GrpcRequest.class, grpcRequest -> {
            GrpcSilenceJobRequest grpcSilenceJobRequest = grpcRequest.getGrpcSilenceJobRequest();
            Metadata metadata = grpcSilenceJobRequest.getMetadata();
            String uri = metadata.getUri();
            if (StrUtil.isBlank(uri)) {
                SilenceJobLog.LOCAL.error("uri can not be null");
                return;
            }

            Map<String, String> headersMap = metadata.getHeadersMap();
            SilenceJobRpcResult SilenceJobRpcResult = null;
            try {
                SilenceJobRequest request = new SilenceJobRequest();
                String body = grpcSilenceJobRequest.getBody();
                Object[] objects = JSON.parseObject(body, Object[].class);
                request.setArgs(objects);
                request.setReqId(grpcSilenceJobRequest.getReqId());
                SilenceJobRpcResult = doProcess(uri, JSON.toJSONString(request), headersMap);
                if (Objects.isNull(SilenceJobRpcResult)) {
                    SilenceJobRpcResult = new SilenceJobRpcResult(500, "服务端异常", null,
                        grpcSilenceJobRequest.getReqId());
                }
            } catch (Exception e) {
                SilenceJobLog.LOCAL.error("http request error. [{}]", grpcSilenceJobRequest, e);
                SilenceJobRpcResult = new SilenceJobRpcResult(500, e.getMessage(), null,
                    grpcSilenceJobRequest.getReqId());
            } finally {
                StreamObserver<GrpcResult> streamObserver = grpcRequest.getStreamObserver();
                GrpcResult grpcResult = GrpcResult.newBuilder()
                    .setReqId(SilenceJobRpcResult.getReqId())
                    .setStatus(SilenceJobRpcResult.getCode())
                    .setMessage(Optional.ofNullable(SilenceJobRpcResult.getMessage()).orElse(StrUtil.EMPTY))
                    .setData(JSON.toJSONString(SilenceJobRpcResult.getData()))
                    .build();
                streamObserver.onNext(grpcResult);
                streamObserver.onCompleted();
                getContext().stop(getSelf());
            }


        }).build();
    }

    private SilenceJobRpcResult doProcess(String uri, String content, Map<String, String> headersMap) {
        String groupName = headersMap.get(HeadersEnum.GROUP_NAME.getKey());
        String namespace = headersMap.get(HeadersEnum.NAMESPACE.getKey());
        String token = headersMap.get(HeadersEnum.TOKEN.getKey());

        if (StrUtil.isBlank(token) || !CacheToken.get(groupName, namespace).equals(token)) {
            throw CommonErrors.ACCESS_DENIED.createException("Token authentication failed. [namespace:{} groupName:{} token:{}]",
                    namespace, groupName, token);
        }

        DefaultHttpHeaders headers = new DefaultHttpHeaders();
        headersMap.forEach(headers::add);

        UrlBuilder builder = UrlBuilder.ofHttp(uri);
        Collection<HttpRequestHandler> httpRequestHandlers = SilenceSpringContext.getContext()
            .getBeansOfType(HttpRequestHandler.class).values();
        for (HttpRequestHandler httpRequestHandler : httpRequestHandlers) {
            if (httpRequestHandler.supports(builder.getPathStr())) {
                return httpRequestHandler.doHandler(content, builder, headers);
            }
        }

        throw CommonErrors.INVALID_PARAMETER.createException("No matching handler found. Path:[{}] method:[{}]", builder.getPathStr());
    }


}
