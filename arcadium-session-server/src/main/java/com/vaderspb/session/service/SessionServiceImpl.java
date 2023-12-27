package com.vaderspb.session.service;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import com.vaderspb.session.proto.CreateSessionRequest;
import com.vaderspb.session.proto.CreateSessionResponse;
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
            final String sessionId = RandomStringUtils.random(16, "abcdefghijklmnopqrstuvwxyz") +
                    TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

            final Jinjava jinjava = new Jinjava();
            final String renderedWorkerConfig = jinjava.render(
                    workerConfig,
                    ImmutableMap.of("workerId", sessionId)
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
}
