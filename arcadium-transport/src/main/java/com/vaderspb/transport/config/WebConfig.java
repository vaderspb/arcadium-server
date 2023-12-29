package com.vaderspb.transport.config;

import com.google.common.collect.ImmutableMap;
import com.vaderspb.transport.service.WorkerGameService;
import com.vaderspb.transport.web.ControlWebSocketHandler;
import com.vaderspb.transport.web.VideoWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

@Configuration
public class WebConfig {
    @Bean
    public HandlerMapping handlerMapping(final WebSocketHandler videoWebSocketHandler,
                                         final WebSocketHandler controlWebSocketHandler) {
        return new SimpleUrlHandlerMapping(
                ImmutableMap.of(
                        "/video_ws", videoWebSocketHandler,
                        "/control_ws", controlWebSocketHandler
                ),
                -1
        );
    }

    @Bean
    public WebSocketHandler videoWebSocketHandler(final WorkerGameService workerGameService) {
        return new VideoWebSocketHandler(workerGameService);
    }

    @Bean
    public WebSocketHandler controlWebSocketHandler(final WorkerGameService workerGameService) {
        return new ControlWebSocketHandler(workerGameService);
    }
}
