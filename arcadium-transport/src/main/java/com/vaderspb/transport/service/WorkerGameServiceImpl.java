package com.vaderspb.transport.service;

import com.vaderspb.session.proto.GetSessionInfoRequest;
import com.vaderspb.session.proto.GetSessionInfoResponse;
import com.vaderspb.session.proto.SessionServiceGrpc;
import com.vaderspb.worker.proto.GameInterfaceGrpc;
import com.vaderspb.worker.proto.VideoFrame;
import com.vaderspb.worker.proto.VideoSettings;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import static com.google.common.base.Preconditions.checkNotNull;

public class WorkerGameServiceImpl implements WorkerGameService {
    private final SessionServiceGrpc.SessionServiceStub sessionService;

    public WorkerGameServiceImpl(final SessionServiceGrpc.SessionServiceStub sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public Flux<VideoFrame> videoChannel(final String sessionId, final VideoSettings videoSettings) {
        checkNotNull(sessionId);
        checkNotNull(videoSettings);

        final Mono<GetSessionInfoResponse> sessionInfo =
                Mono.create((final MonoSink<GetSessionInfoResponse> monoSink) -> sessionService.getSessionInfo(
                        GetSessionInfoRequest.newBuilder()
                                .setId(sessionId)
                                .build(),
                        new MonoSinkObserver<>(monoSink)
                ));

        return sessionInfo
                .map(GetSessionInfoResponse::getAddress)
                .flatMapMany(address -> connectToVideoStream(address, videoSettings));
    }

    private Flux<VideoFrame> connectToVideoStream(final String address, final VideoSettings videoSettings) {
        final ManagedChannel managedChannel =
                Grpc.newChannelBuilder(address, InsecureChannelCredentials.create())
                        .build();

        final GameInterfaceGrpc.GameInterfaceStub gameInterfaceStub =
                GameInterfaceGrpc.newStub(managedChannel);

        return Flux.create((final FluxSink<VideoFrame> fluxSink) -> {
            final StreamObserver<VideoSettings> settingsObserver =
                    gameInterfaceStub.videoChannel(new FluxSinkObserver<>(fluxSink));

            settingsObserver.onNext(videoSettings);

            fluxSink.onCancel(settingsObserver::onCompleted);
        });
    }

    private static class FluxSinkObserver<T> implements StreamObserver<T> {
        private final FluxSink<T> sink;

        private FluxSinkObserver(final FluxSink<T> sink) {
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

    private static class MonoSinkObserver<T> implements StreamObserver<T> {
        private final MonoSink<T> sink;

        private MonoSinkObserver(final MonoSink<T> sink) {
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
}
