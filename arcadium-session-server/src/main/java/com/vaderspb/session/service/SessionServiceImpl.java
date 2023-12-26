package com.vaderspb.session.service;

import com.vaderspb.session.proto.CreateSessionRequest;
import com.vaderspb.session.proto.CreateSessionResponse;
import com.vaderspb.session.proto.SessionServiceGrpc;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.grpc.stub.StreamObserver;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class SessionServiceImpl extends SessionServiceGrpc.SessionServiceImplBase {
    private final KubernetesClient kubernetesClient;
    private final String workerConfig;

    public SessionServiceImpl(final KubernetesClient kubernetesClient,
                              final String workerConfig) {
        this.kubernetesClient = kubernetesClient;
        this.workerConfig = workerConfig;
    }

    @Override
    public void createSession(final CreateSessionRequest request,
                              final StreamObserver<CreateSessionResponse> responseObserver) {
        try {
            kubernetesClient.load(new ByteArrayInputStream(workerConfig.getBytes(StandardCharsets.UTF_8))).create();

            responseObserver.onNext(CreateSessionResponse.newBuilder().setId("sessionId").build());
        } catch (final Throwable e) {
            responseObserver.onError(e);
        }
        responseObserver.onCompleted();
    }
}
