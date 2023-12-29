package com.vaderspb.session.service;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import com.vaderspb.session.config.WorkerProperties;
import com.vaderspb.session.proto.CreateSessionRequest;
import com.vaderspb.session.proto.CreateSessionResponse;
import com.vaderspb.session.proto.GetSessionInfoRequest;
import com.vaderspb.session.proto.GetSessionInfoResponse;
import com.vaderspb.session.proto.SessionServiceGrpc;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class SessionServiceImpl extends SessionServiceGrpc.SessionServiceImplBase {
    private static final Logger LOG = LoggerFactory.getLogger(SessionServiceImpl.class);

    private final KubernetesClient kubernetesClient;
    private final WorkerProperties workerProperties;
    private final String workerConfigTemplate;

    public SessionServiceImpl(final KubernetesClient kubernetesClient,
                              final WorkerProperties workerProperties,
                              final String workerConfigTemplate) {
        this.kubernetesClient = kubernetesClient;
        this.workerProperties = workerProperties;
        this.workerConfigTemplate = workerConfigTemplate;
    }

    @Override
    public void createSession(final CreateSessionRequest request,
                              final StreamObserver<CreateSessionResponse> responseObserver) {
        try {
            final String sessionId = RandomStringUtils.random(16, "abcdefghijklmnopqrstuvwxyz") +
                    TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

            final Jinjava jinjava = new Jinjava();
            final String renderedWorkerConfig = jinjava.render(
                    workerConfigTemplate,
                    ImmutableMap.of(
                            "workerId", sessionId,
                            "workerConfig", workerProperties
                    )
            );

            kubernetesClient.load(new ByteArrayInputStream(
                    renderedWorkerConfig.getBytes(StandardCharsets.UTF_8)
            )).create();

            responseObserver.onNext(CreateSessionResponse.newBuilder()
                    .setId(sessionId)
                    .build()
            );
            responseObserver.onCompleted();
        } catch (final Throwable e) {
            LOG.warn("Unable to start a session", e);

            responseObserver.onError(e);
        }
    }

    @Override
    public void getSessionInfo(final GetSessionInfoRequest request,
                               final StreamObserver<GetSessionInfoResponse> responseObserver) {
        responseObserver.onNext(GetSessionInfoResponse.newBuilder()
                .setId(request.getId())
                .setAddress(request.getId() + ".arcadium-worker:8080")
                .build()
        );
        responseObserver.onCompleted();
    }
}
