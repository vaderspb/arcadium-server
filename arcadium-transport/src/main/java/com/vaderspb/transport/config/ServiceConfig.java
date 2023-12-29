package com.vaderspb.transport.config;

import com.vaderspb.session.proto.SessionServiceGrpc;
import com.vaderspb.transport.service.WorkerGameService;
import com.vaderspb.transport.service.WorkerGameServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {
    @Bean
    public WorkerGameService workerGameService(final SessionServiceGrpc.SessionServiceStub sessionService) {
        return new WorkerGameServiceImpl(sessionService);
    }
}
