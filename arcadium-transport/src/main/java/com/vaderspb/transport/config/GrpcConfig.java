package com.vaderspb.transport.config;

import com.vaderspb.session.proto.SessionServiceGrpc;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfig {
    @Bean
    public SessionServiceGrpc.SessionServiceStub sessionService(final ManagedChannel sessionServerChannel) {
        return SessionServiceGrpc.newStub(sessionServerChannel);
    }

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel sessionServerChannel() {
        return Grpc.newChannelBuilder("arcadium-session-server-service:8080", InsecureChannelCredentials.create())
                .build();
    }
}
