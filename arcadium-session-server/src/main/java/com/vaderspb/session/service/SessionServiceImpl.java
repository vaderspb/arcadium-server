package com.vaderspb.session.service;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import com.vaderspb.session.proto.CreateSessionRequest;
import com.vaderspb.session.proto.CreateSessionResponse;
import com.vaderspb.session.proto.SessionServiceGrpc;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.grpc.stub.StreamObserver;
import io.micrometer.core.instrument.util.TimeUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

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
            final String sessionId = RandomStringUtils.randomAlphabetic(16) +
                    TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

            final Jinjava jinjava = new Jinjava();
            final String renderedWorkerConfig = jinjava.render(workerConfig, ImmutableMap.of("workerId", sessionId));

            kubernetesClient.load(new ByteArrayInputStream(renderedWorkerConfig.getBytes(StandardCharsets.UTF_8))).create();

            responseObserver.onNext(CreateSessionResponse.newBuilder()
                    .setId(sessionId)
                    .build()
            );
        } catch (final Throwable e) {
            responseObserver.onError(e);
        }
        responseObserver.onCompleted();
    }
}
