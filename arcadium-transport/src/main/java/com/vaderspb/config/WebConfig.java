package com.vaderspb.config;

import com.google.common.collect.ImmutableMap;
import com.vaderspb.web.VideoWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

@Configuration
public class WebConfig {
    @Bean
    public HandlerMapping handlerMapping(final WebSocketHandler gameWebSocketHandler) {
        return new SimpleUrlHandlerMapping(
                ImmutableMap.of(
                        "/video_ws", gameWebSocketHandler
                ),
                -1
        );
    }

    @Bean
    public WebSocketHandler videoWebSocketHandler() {
        return new VideoWebSocketHandler();
    }
}
