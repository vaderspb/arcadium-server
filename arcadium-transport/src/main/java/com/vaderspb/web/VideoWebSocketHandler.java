package com.vaderspb.web;

import com.vaderspb.worker.proto.GameInterfaceGrpc;
import com.vaderspb.worker.proto.VideoFrame;
import com.vaderspb.worker.proto.VideoQuality;
import com.vaderspb.worker.proto.VideoSettings;
import io.grpc.stub.StreamObserver;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class VideoWebSocketHandler implements WebSocketHandler {
    private final GameInterfaceGrpc.GameInterfaceStub gameInterfaceStub;

    public VideoWebSocketHandler(final GameInterfaceGrpc.GameInterfaceStub gameInterfaceStub) {
        this.gameInterfaceStub = gameInterfaceStub;
    }

    @Override
    public Mono<Void> handle(final WebSocketSession session) {
        final VideoStreamObserver videoStreamObserver = new VideoStreamObserver();

        gameInterfaceStub.videoChannel(videoStreamObserver)
                .onNext(VideoSettings.newBuilder()
                        .setQuality(VideoQuality.HIGH)
                        .build()
                );

        final Mono<Void> sendingCompletion = session.send(
                videoStreamObserver.getAsFlux()
                        .map(frame -> toWsMessage(session, frame))
        );

        Mono<Void> receivingCompletion = session.receive()
                .doOnNext(wsMessage -> {
                    // Do the joystick commands
                    System.out.println(wsMessage);
                })
                .then();

        return Mono.zip(receivingCompletion, sendingCompletion).then();
    }

    private static class VideoStreamObserver implements StreamObserver<VideoFrame> {
        @Override
        public void onNext(final VideoFrame value) {
        }

        @Override
        public void onError(final Throwable th) {
        }

        @Override
        public void onCompleted() {
        }

        public Flux<VideoFrame> getAsFlux() {
            return Flux.just();
        }
    }

    private static WebSocketMessage toWsMessage(final WebSocketSession session,
                                                final VideoFrame videoFrame) {
        return session.textMessage("");
    }
}
