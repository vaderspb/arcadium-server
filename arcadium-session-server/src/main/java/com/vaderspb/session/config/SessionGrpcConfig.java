package com.vaderspb.session.config;

import com.vaderspb.session.proto.SessionServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class SessionGrpcConfig {
    @Bean(destroyMethod = "shutdown")
    public Server grpcServer(final SessionServiceGrpc.SessionServiceImplBase sessionService) throws IOException {
        return ServerBuilder.forPort(8080)
                .addService(sessionService)
                .build()
                .start();
    }
}
