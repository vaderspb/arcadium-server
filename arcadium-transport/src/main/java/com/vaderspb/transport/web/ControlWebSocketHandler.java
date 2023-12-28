package com.vaderspb.transport.web;

import com.google.common.collect.Iterables;
import com.vaderspb.transport.service.WorkerGameService;
import com.vaderspb.worker.proto.ControlButton;
import com.vaderspb.worker.proto.ControlJoystick;
import com.vaderspb.worker.proto.ControlRequest;
import com.vaderspb.worker.proto.ControlState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

public class ControlWebSocketHandler implements WebSocketHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ControlWebSocketHandler.class);

    private final WorkerGameService workerGameService;

    public ControlWebSocketHandler(final WorkerGameService workerGameService) {
        this.workerGameService = workerGameService;
    }

    @Override
    public Mono<Void> handle(final WebSocketSession session) {
        LOG.info("New control session connected: {}", session.getHandshakeInfo().getUri());

        final String sessionId = Iterables.getOnlyElement(
                UriComponentsBuilder.fromUri(session.getHandshakeInfo().getUri())
                        .build()
                        .getQueryParams()
                        .get("sessionId")
        );

        return workerGameService.controlChannel(
                sessionId,
                session.receive().map(this::toControlMessage)
        );
    }

    private ControlRequest toControlMessage(final WebSocketMessage webSocketMessage) {
        return ControlRequest.newBuilder()
                .setControllerId(ControlJoystick.JOYSTICK1)
                .setButton(ControlButton.B)
                .setState(ControlState.PRESSED)
                .build();
    }
}
