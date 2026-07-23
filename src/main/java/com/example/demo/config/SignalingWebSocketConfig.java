package com.example.demo.config;

import com.example.demo.websocket.WebRTCSignalingHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class SignalingWebSocketConfig implements WebSocketConfigurer {

    private final WebRTCSignalingHandler signalingHandler;

    public SignalingWebSocketConfig(WebRTCSignalingHandler signalingHandler) {
        this.signalingHandler = signalingHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(signalingHandler, "/signaling")
                .setAllowedOrigins("*");
    }
}
