package com.vaderspb.session.config;

import com.vaderspb.session.proto.SessionServiceGrpc;
import com.vaderspb.session.service.SessionServiceImpl;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionServiceConfig {
    @Bean
    public SessionServiceGrpc.SessionServiceImplBase sessionService(final KubernetesClient kubernetesClient) {
        return new SessionServiceImpl(kubernetesClient);
    }

    @Bean
    public KubernetesClient kubernetesClient() {
        return new KubernetesClientBuilder()
                .build();
    }
}
