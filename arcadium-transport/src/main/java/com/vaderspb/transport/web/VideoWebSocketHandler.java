package com.vaderspb.transport.web;

import com.google.common.collect.Iterables;
import com.vaderspb.transport.service.WorkerGameService;
import com.vaderspb.worker.proto.VideoFrame;
import com.vaderspb.worker.proto.VideoQuality;
import com.vaderspb.worker.proto.VideoSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

public class VideoWebSocketHandler implements WebSocketHandler {
    private static final Logger LOG = LoggerFactory.getLogger(VideoWebSocketHandler.class);

    private final WorkerGameService workerGameService;

    public VideoWebSocketHandler(final WorkerGameService workerGameService) {
        this.workerGameService = workerGameService;
    }

    @Override
    public Mono<Void> handle(final WebSocketSession session) {
        LOG.info("New session connected: {}", session.getHandshakeInfo().getUri());

        final String sessionId = Iterables.getOnlyElement(
                UriComponentsBuilder.fromUri(session.getHandshakeInfo().getUri())
                        .build()
                        .getQueryParams()
                        .get("sessionId")
        );

        final VideoSettings videoSettings = VideoSettings.newBuilder()
                .setQuality(VideoQuality.HIGH)
                .build();

        final Mono<Void> sendingCompletion = session.send(
                workerGameService.videoChannel(sessionId, videoSettings)
                        .map(frame -> toVideoMessage(session, frame))
        );

        return Mono.zip(session.receive().then(), sendingCompletion).then();
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
