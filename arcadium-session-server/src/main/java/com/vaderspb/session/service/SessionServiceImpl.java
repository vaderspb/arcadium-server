package com.vaderspb.session.service;

import com.vaderspb.session.proto.CreateSessionRequest;
import com.vaderspb.session.proto.CreateSessionResponse;
import com.vaderspb.session.proto.SessionServiceGrpc;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.grpc.stub.StreamObserver;

public class SessionServiceImpl extends SessionServiceGrpc.SessionServiceImplBase {
    private final KubernetesClient kubernetesClient;

    public SessionServiceImpl(final KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    @Override
    public void createSession(final CreateSessionRequest request,
                              final StreamObserver<CreateSessionResponse> responseObserver) {
        kubernetesClient.load(SessionServiceImpl.class.getResourceAsStream("/test-resource-list.yaml"))
                .inNamespace("default")
                .create();

        responseObserver.onNext(CreateSessionResponse.newBuilder().setId("sessionId").build());
        responseObserver.onCompleted();
    }
}
