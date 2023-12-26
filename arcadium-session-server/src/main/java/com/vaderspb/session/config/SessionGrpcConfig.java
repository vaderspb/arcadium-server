package com.vaderspb.session.config;

import com.vaderspb.session.proto.SessionServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class SessionGrpcConfig {
    @Bean(destroyMethod = "shutdown")
    public ExecutorService grpcExecutorService() {
        return new ThreadPoolExecutor(200, 200,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(200)
        );
    }

    @Bean(destroyMethod = "shutdown")
    public Server grpcServer(final SessionServiceGrpc.SessionServiceImplBase sessionService,
                             final ExecutorService grpcExecutorService) throws IOException {
        return ServerBuilder.forPort(8080)
                .addService(sessionService)
                .executor(grpcExecutorService)
                .build()
                .start();
    }
}
