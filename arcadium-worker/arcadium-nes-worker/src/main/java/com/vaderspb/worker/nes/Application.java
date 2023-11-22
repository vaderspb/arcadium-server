package com.vaderspb.worker.nes;

import com.vaderspb.worker.nes.engine.NesEngineImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

public class Application {
    public static void main(final String[] args) throws IOException, InterruptedException, ExecutionException {
        final String romFile = System.getenv("ROM_FILE");

        try (final NesEngineImpl nesEngine = new NesEngineImpl(romFile);
             final AdminInterfaceImpl adminInterface = new AdminInterfaceImpl(nesEngine, Duration.ofSeconds(300))) {

            final GameInterfaceImpl gameInterface =
                    new GameInterfaceImpl(nesEngine);

            final Server appServer = ServerBuilder.forPort(8080)
                    .addService(adminInterface)
                    .addService(gameInterface)
                    .build();

            appServer.start();
            nesEngine.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                nesEngine.shutdown();
                appServer.shutdown();
            }));

            nesEngine.awaitTermination();

            appServer.shutdown();
            appServer.awaitTermination();
        }
    }
}
