package com.vaderspb.transport.service;

import com.google.protobuf.Empty;
import com.vaderspb.session.proto.GetSessionInfoRequest;
import com.vaderspb.session.proto.GetSessionInfoResponse;
import com.vaderspb.session.proto.SessionServiceGrpc;
import com.vaderspb.worker.proto.ControlRequest;
import com.vaderspb.worker.proto.GameInterfaceGrpc;
import com.vaderspb.worker.proto.VideoFrame;
import com.vaderspb.worker.proto.VideoSettings;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import static com.google.common.base.Preconditions.checkNotNull;

public class WorkerGameServiceImpl implements WorkerGameService {
    private static final Logger LOG = LoggerFactory.getLogger(WorkerGameServiceImpl.class);

    private final SessionServiceGrpc.SessionServiceStub sessionService;

    public WorkerGameServiceImpl(final SessionServiceGrpc.SessionServiceStub sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public Flux<VideoFrame> videoChannel(final String sessionId,
                                         final VideoSettings videoSettings) {
        checkNotNull(sessionId);
        checkNotNull(videoSettings);

        final Mono<GetSessionInfoResponse> sessionInfo = getSessionInfo(sessionId);

        return sessionInfo
                .map(GetSessionInfoResponse::getAddress)
                .flatMapMany(address -> connectToVideoStream(address, videoSettings));
    }

    private Mono<GetSessionInfoResponse> getSessionInfo(final String sessionId) {
        return Mono.create((final MonoSink<GetSessionInfoResponse> monoSink) -> sessionService.getSessionInfo(
                GetSessionInfoRequest.newBuilder()
                        .setId(sessionId)
                        .build(),
                new MonoSourceObserver<>(monoSink)
        ));
    }

    private Flux<VideoFrame> connectToVideoStream(final String address,
                                                  final VideoSettings videoSettings) {
        final ManagedChannel managedChannel =
                Grpc.newChannelBuilder(address, InsecureChannelCredentials.create())
                        .build();

        final GameInterfaceGrpc.GameInterfaceStub gameInterfaceStub =
                GameInterfaceGrpc.newStub(managedChannel);

        return Flux.create((final FluxSink<VideoFrame> sink) -> {
            final StreamObserver<VideoSettings> settingsObserver =
                    gameInterfaceStub.videoChannel(new FluxSourceObserver<>(sink));

            settingsObserver.onNext(videoSettings);

            sink.onDispose(() -> {
                try {
                    settingsObserver.onCompleted();
                } catch (final Exception e) {
                    LOG.warn("Unable to stop server stream", e);
                }
                try {
                    managedChannel.shutdown();
                } catch (final Exception e) {
                    LOG.warn("Unable to stop Grpc channel", e);
                }
            });
        });
    }

    private static class FluxSourceObserver<T> implements StreamObserver<T> {
        private final FluxSink<T> sink;

        private FluxSourceObserver(final FluxSink<T> sink) {
            this.sink = sink;
        }

        @Override
        public void onNext(final T value) {
            sink.next(value);
        }

        @Override
        public void onError(final Throwable th) {
            sink.error(th);
        }

        @Override
        public void onCompleted() {
            sink.complete();
        }
    }

    private static class MonoSourceObserver<T> implements StreamObserver<T> {
        private final MonoSink<T> sink;

        private MonoSourceObserver(final MonoSink<T> sink) {
            this.sink = sink;
        }

        @Override
        public void onNext(final T value) {
            sink.success(value);
        }

        @Override
        public void onError(final Throwable th) {
            sink.error(th);
        }

        @Override
        public void onCompleted() {
        }
    }

    @Override
    public Mono<Void> controlChannel(final String sessionId,
                                     final Flux<ControlRequest> controlRequests) {
        checkNotNull(sessionId);
        checkNotNull(controlRequests);

        final Mono<GetSessionInfoResponse> sessionInfo = getSessionInfo(sessionId);

        return sessionInfo
                .map(GetSessionInfoResponse::getAddress)
                .flatMap(address -> {
                    final ManagedChannel managedChannel =
                            Grpc.newChannelBuilder(address, InsecureChannelCredentials.create())
                                    .build();

                    final GameInterfaceGrpc.GameInterfaceStub gameInterfaceStub =
                            GameInterfaceGrpc.newStub(managedChannel);

                    return Mono.<Empty>create(sink -> {
                        final StreamObserver<ControlRequest> controlRequestStreamObserver =
                                gameInterfaceStub.controlChannel(new MonoSinkStreamObserver<>(sink));

                        final Disposable controllerRequestsHandle =
                                controlRequests.subscribe(
                                        controlRequestStreamObserver::onNext,
                                        error -> LOG.warn("Error in control stream", error),
                                        () -> {
                                            try {
                                                controlRequestStreamObserver.onCompleted();
                                            } catch (final Exception e) {
                                                LOG.warn("Unable to stop server stream", e);
                                            }
                                            try {
                                                managedChannel.shutdown();
                                            } catch (final Exception e) {
                                                LOG.warn("Unable to stop server Grpc channel", e);
                                            }
                                        }
                                );

                        sink.onDispose(() -> {
                            try {
                                controllerRequestsHandle.dispose();
                            } catch (final Exception e) {
                                LOG.warn("Unable to stop client stream", e);
                            }
                        });
                    });
                })
                .then();
    }

    private static class MonoSinkStreamObserver<T> implements StreamObserver<T> {
        private final MonoSink<T> monoSink;

        private MonoSinkStreamObserver(final MonoSink<T> monoSink) {
            this.monoSink = monoSink;
        }

        @Override
        public void onNext(final T value) {
        }
        @Override
        public void onError(final Throwable t) {
            monoSink.error(t);
        }
        @Override
        public void onCompleted() {
        }
    }
}
