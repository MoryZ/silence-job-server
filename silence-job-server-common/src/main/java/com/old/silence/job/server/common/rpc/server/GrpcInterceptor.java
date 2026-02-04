package com.old.silence.job.server.common.rpc.server;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GrpcInterceptor implements ServerInterceptor {


    private static final Logger log = LoggerFactory.getLogger(GrpcInterceptor.class);

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String fullMethodName = serverCall.getMethodDescriptor().getFullMethodName();
        long start = System.currentTimeMillis();
        Context context = Context.current();

        try {
            return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
        } finally {
            log.debug("method invoked: {} cast:{}ms", fullMethodName, System.currentTimeMillis() - start);
        }
    }

}
