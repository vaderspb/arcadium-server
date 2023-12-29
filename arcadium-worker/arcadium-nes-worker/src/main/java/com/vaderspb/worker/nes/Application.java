package com.vaderspb.worker.nes;

import com.vaderspb.worker.nes.codec.NesCompressingCodec;
import com.vaderspb.worker.nes.engine.NesEngineImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Application {
    public static void main(final String[] args) throws IOException, InterruptedException, ExecutionException {
        final String romFile = System.getenv("ROM_FILE");
        final Duration inactivityDuration = Duration.parse(System.getenv("INACTIVITY_DURATION"));

        try (final NesEngineImpl nesEngine = new NesEngineImpl(romFile, new NesCompressingCodec());
             final AdminInterfaceImpl adminInterface = new AdminInterfaceImpl(nesEngine, inactivityDuration)) {

            final GameInterfaceImpl gameInterface =
                    new GameInterfaceImpl(adminInterface, nesEngine);

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
            appServer.awaitTermination(1, TimeUnit.SECONDS);
        }
    }
}
