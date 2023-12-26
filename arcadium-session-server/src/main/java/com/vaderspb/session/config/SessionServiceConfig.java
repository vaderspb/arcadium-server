package com.vaderspb.session.config;

import com.vaderspb.session.proto.SessionServiceGrpc;
import com.vaderspb.session.service.SessionServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionServiceConfig {
    @Bean
    public SessionServiceGrpc.SessionServiceImplBase sessionService() {
        return new SessionServiceImpl();
    }
}
