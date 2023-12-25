package com.vaderspb.session.service;

import com.vaderspb.session.proto.CreateSessionRequest;
import com.vaderspb.session.proto.CreateSessionResponse;
import com.vaderspb.session.proto.SessionServiceGrpc;
import io.grpc.stub.StreamObserver;

public class SessionServiceImpl extends SessionServiceGrpc.SessionServiceImplBase {
    @Override
    public void createSession(final CreateSessionRequest request,
                              final StreamObserver<CreateSessionResponse> responseObserver) {
        responseObserver.onNext(CreateSessionResponse.newBuilder().setId("sessionId").build());
        responseObserver.onCompleted();
    }
}
