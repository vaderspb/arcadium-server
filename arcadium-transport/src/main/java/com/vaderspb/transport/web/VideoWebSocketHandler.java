package com.vaderspb.transport.web;

import com.vaderspb.worker.proto.GameInterfaceGrpc;
import com.vaderspb.worker.proto.VideoFrame;
import com.vaderspb.worker.proto.VideoQuality;
import com.vaderspb.worker.proto.VideoSettings;
import io.grpc.stub.StreamObserver;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

public class VideoWebSocketHandler implements WebSocketHandler {
    private final GameInterfaceGrpc.GameInterfaceStub gameInterfaceStub;

    public VideoWebSocketHandler(final GameInterfaceGrpc.GameInterfaceStub gameInterfaceStub) {
        this.gameInterfaceStub = gameInterfaceStub;
    }

    @Override
    public Mono<Void> handle(final WebSocketSession session) {
        final Mono<Void> sendingCompletion = session.send(
                Flux.create(this::connectToVideoStream)
                        .map(frame -> toVideoMessage(session, frame))
        );

        final Mono<Void> receivingCompletion = session.receive()
                .then();

        return Mono.zip(receivingCompletion, sendingCompletion)
                .then();
    }

    private void connectToVideoStream(final FluxSink<VideoFrame> emitter) {
        final StreamObserver<VideoSettings> settingsObserver =
                gameInterfaceStub.videoChannel(new VideoStreamObserver(emitter));

        settingsObserver.onNext(VideoSettings.newBuilder()
                .setQuality(VideoQuality.HIGH)
                .build()
        );

        emitter.onCancel(settingsObserver::onCompleted);
    }

    private static class VideoStreamObserver implements StreamObserver<VideoFrame> {
        private final FluxSink<VideoFrame> sink;

        private VideoStreamObserver(final FluxSink<VideoFrame> sink) {
            this.sink = sink;
        }

        @Override
        public void onNext(final VideoFrame value) {
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

    private static WebSocketMessage toVideoMessage(final WebSocketSession session,
                                                   final VideoFrame videoFrame) {
        return session.binaryMessage(bufferFactory -> {
            final DataBuffer dataBuffer = bufferFactory.allocateBuffer(
                    videoFrame.getData().size() + 1
            );
            dataBuffer.write((byte) videoFrame.getType().getNumber());
            for (int i = 0; i < videoFrame.getData().size(); i++) {
                final byte datum = videoFrame.getData().byteAt(i);
                dataBuffer.write(datum);
            }
            return dataBuffer;
        });
    }
}
