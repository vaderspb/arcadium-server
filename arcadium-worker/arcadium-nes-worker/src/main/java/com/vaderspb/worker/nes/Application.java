package com.vaderspb.worker.nes;

import com.vaderspb.worker.nes.engine.NesEngineImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Application {
    public static void main(final String[] args) throws IOException, InterruptedException, ExecutionException {
        final String romFile = System.getenv("ROM_FILE");

        final NesEngineImpl nesEngine = new NesEngineImpl(romFile);

        final Server appServer = ServerBuilder.forPort(8080)
                .addService(new WorkerInterfaceImpl(nesEngine))
                .build();

        appServer.start();
        nesEngine.start();

        appServer.awaitTermination();
        nesEngine.awaitTermination();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            appServer.shutdown();
            nesEngine.shutdown();
        }));
    }
}
