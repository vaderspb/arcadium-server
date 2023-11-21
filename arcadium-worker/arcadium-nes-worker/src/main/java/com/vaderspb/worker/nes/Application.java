package com.vaderspb.worker.nes;

import com.vaderspb.worker.nes.engine.NesEngineImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class Application {
    public static void main(final String[] args) throws IOException {
        final String romFile = System.getenv("ROM_FILE");

        final Server appServer = ServerBuilder.forPort(8080)
                .addService(new WorkerInterfaceImpl(new NesEngineImpl(romFile)))
                .build();

        appServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            appServer.shutdown();
            try {
                appServer.awaitTermination();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
    }
}
