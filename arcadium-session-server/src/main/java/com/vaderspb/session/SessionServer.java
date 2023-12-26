package com.vaderspb.session;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SessionServer {
    public static void main(final String[] args) {
        final SpringApplication springApplication = new SpringApplication(SessionServer.class);
        springApplication.setKeepAlive(true);
        springApplication.run(args);
    }
}
