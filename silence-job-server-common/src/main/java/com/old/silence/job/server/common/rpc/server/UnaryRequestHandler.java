package com.old.silence.job.server.common.rpc.server;

import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import org.apache.pekko.actor.ActorRef;
import com.old.silence.job.common.grpc.auto.GrpcResult;
import com.old.silence.job.common.grpc.auto.GrpcSilenceJobRequest;
import com.old.silence.job.common.grpc.auto.Metadata;
import com.old.silence.job.server.common.dto.GrpcRequest;
import com.old.silence.job.server.common.pekko.ActorGenerator;


public class UnaryRequestHandler implements ServerCalls.UnaryMethod<GrpcSilenceJobRequest, GrpcResult>{

    @Override
    public void invoke(GrpcSilenceJobRequest grpcSilenceJobRequest, StreamObserver<GrpcResult> streamObserver) {
        Metadata metadata = grpcSilenceJobRequest.getMetadata();
        GrpcRequest grpcRequest = new GrpcRequest();
        grpcRequest.setUri(metadata.getUri());
        grpcRequest.setGrpcSilenceJobRequest(grpcSilenceJobRequest);
        grpcRequest.setStreamObserver(streamObserver);

        ActorRef actorRef = ActorGenerator.requestGrpcHandlerActor();
        actorRef.tell(grpcRequest, actorRef);
    }
}
