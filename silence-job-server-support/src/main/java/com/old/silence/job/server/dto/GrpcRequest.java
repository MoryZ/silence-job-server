package com.old.silence.job.server.common.dto;


import io.grpc.stub.StreamObserver;
import com.old.silence.job.common.grpc.auto.GrpcResult;
import com.old.silence.job.common.grpc.auto.GrpcSilenceJobRequest;



/**
 * netty客户端请求模型
 *
 */

public class GrpcRequest {

    private GrpcSilenceJobRequest grpcSilenceJobRequest;

    private StreamObserver<GrpcResult> streamObserver;

    private String uri;

    public GrpcSilenceJobRequest getGrpcSilenceJobRequest() {
        return grpcSilenceJobRequest;
    }

    public void setGrpcSilenceJobRequest(GrpcSilenceJobRequest grpcSilenceJobRequest) {
        this.grpcSilenceJobRequest = grpcSilenceJobRequest;
    }

    public StreamObserver<GrpcResult> getStreamObserver() {
        return streamObserver;
    }

    public void setStreamObserver(StreamObserver<GrpcResult> streamObserver) {
        this.streamObserver = streamObserver;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
