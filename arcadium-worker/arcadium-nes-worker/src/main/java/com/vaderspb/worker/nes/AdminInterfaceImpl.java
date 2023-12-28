package com.vaderspb.worker.nes;

import com.google.protobuf.Empty;
import com.vaderspb.worker.nes.engine.NesEngine;
import com.vaderspb.worker.proto.AdminInterfaceGrpc;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class AdminInterfaceImpl extends AdminInterfaceGrpc.AdminInterfaceImplBase implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(AdminInterfaceImpl.class);

    private final NesEngine nesEngine;
    private final Duration inactivityDuration;
    private final AtomicLong terminationNanos = new AtomicLong();
    private final Timer timer = new Timer();

    public AdminInterfaceImpl(final NesEngine nesEngine,
                              final Duration inactivityDuration) {
        this.nesEngine = nesEngine;
        this.inactivityDuration = inactivityDuration;

        scheduleTermination();

        timer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        maybeTerminate();
                    }
                },
                TimeUnit.SECONDS.toMillis(5),
                TimeUnit.SECONDS.toMillis(5)
        );
    }

    @Override
    public void ping(final Empty request,
                     final StreamObserver<Empty> responseObserver) {
        scheduleTermination();

        responseObserver.onCompleted();
    }

    private void scheduleTermination() {
        this.terminationNanos.set(System.nanoTime() + inactivityDuration.toNanos());
    }

    private void maybeTerminate() {
        LOG.info("Scheduled shutdown nanoTime={}", terminationNanos.get());
        if (System.nanoTime() > terminationNanos.get()) {
            LOG.info("Shutting down after the inactivity period of {}", inactivityDuration);
            try {
                nesEngine.shutdown();
            } catch (final Throwable e) {
                LOG.warn("Unable to stop the ROM", e);
            }
        }
    }

    @Override
    public void close() {
        timer.cancel();
    }
}
