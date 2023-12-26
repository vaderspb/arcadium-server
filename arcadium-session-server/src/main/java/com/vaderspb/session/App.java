package com.vaderspb.session;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
    public static void main(final String[] args) {
        final SpringApplication springApplication = new SpringApplication(App.class);
        springApplication.setKeepAlive(true);
        springApplication.run(args);
    }
}
