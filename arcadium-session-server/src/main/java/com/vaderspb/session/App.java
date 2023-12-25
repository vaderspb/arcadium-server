package com.vaderspb.session;

import com.vaderspb.session.service.SessionServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class App {
    public static void main(final String[] args) {
        final Server appServer = ServerBuilder.forPort(8080)
                .addService(new SessionServiceImpl())
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread(appServer::shutdown));
    }
}
