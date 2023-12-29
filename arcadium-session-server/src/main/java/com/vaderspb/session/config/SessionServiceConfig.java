package com.vaderspb.session.config;

import com.vaderspb.session.proto.SessionServiceGrpc;
import com.vaderspb.session.service.SessionServiceImpl;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class SessionServiceConfig {
    @Bean
    public SessionServiceGrpc.SessionServiceImplBase sessionService(
            final KubernetesClient kubernetesClient,
            final WorkerProperties workerProperties,
            @Value("classpath:/k8s/worker.yaml")
            final Resource config) throws IOException {
        return new SessionServiceImpl(
                kubernetesClient,
                workerProperties,
                config.getContentAsString(StandardCharsets.UTF_8)
        );
    }

    @Bean
    public KubernetesClient kubernetesClient() {
        return new KubernetesClientBuilder()
                .build();
    }
}
