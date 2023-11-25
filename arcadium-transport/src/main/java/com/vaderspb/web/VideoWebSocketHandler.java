package com.vaderspb.web;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

public class VideoWebSocketHandler implements WebSocketHandler {
    @Override
    public Mono<Void> handle(final WebSocketSession session) {
        return session.receive()
                .then();
    }
}
