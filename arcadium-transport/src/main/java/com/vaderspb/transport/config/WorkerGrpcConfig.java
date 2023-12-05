package com.vaderspb.transport.config;

import com.vaderspb.worker.proto.GameInterfaceGrpc;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkerGrpcConfig {
    @Bean
    public GameInterfaceGrpc.GameInterfaceStub gameInterfaceStub(final ManagedChannel workerChannel) {
        return GameInterfaceGrpc.newStub(workerChannel);
    }

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel workerChannel() {
        return Grpc.newChannelBuilder("localhost:8080", InsecureChannelCredentials.create())
                .build();
    }
}
