package com.example.IRMS.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketCustomChannelInterceptor authenticationInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // /topic/* broadcasts shared updates (kitchen alerts, menu availability, presence)
        // /queue/* and /user/queue/* are used for user-specific notifications
        config.enableSimpleBroker("/topic", "/queue");
        // Enables private destinations such as /user/queue/alerts
        config.setUserDestinationPrefix("/user"); 
        // Clients publish commands/events to /app/*
        config.setApplicationDestinationPrefixes("/app"); 
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // STOMP endpoint used by kitchen/admin clients.
        registry.addEndpoint("/ws")
        .setAllowedOriginPatterns("*") // Allow all origins for CORS 
        // Fallback options for browsers that don’t support WebSockets (long polling or HTTP streaming)
        .withSockJS();

        // Native WebSocket endpoint for browser clients that do not need SockJS fallbacks.
        registry.addEndpoint("/ws-native")
        .setAllowedOriginPatterns("*");
    }

    // Principal is a functional interface with a single method: String getName();
    // Intercept the CONNECT message to extract and validate the JWT token, then set the authenticated user in the WebSocket session
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authenticationInterceptor);
    }
}
