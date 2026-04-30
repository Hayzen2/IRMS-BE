package com.example.IRMS.config.websocket;
import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import com.example.IRMS.config.jwt.JwtProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketCustomChannelInterceptor implements ChannelInterceptor {
    private final JwtProvider jwtProvider;
    
    // Intercept incoming WebSocket messages to authenticate users based on JWT tokens
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = null;
            
            // Try to get token from cookie first
            String cookieHeader = accessor.getFirstNativeHeader("Cookie");
            if (cookieHeader != null) {
                for (String cookie : cookieHeader.split(";")) {
                    String trimmed = cookie.trim();
                    if (trimmed.startsWith("access_token=")) {
                        token = trimmed.substring("access_token=".length());
                        break;
                    }
                }
            }
            
            // Fallback to Bearer header
            if (token == null) {
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7); // Remove "Bearer " prefix
                }
            }
            
            if (token != null && jwtProvider.validateToken(token)) {
                String userId = jwtProvider.getUserIdFromToken(token);
                String role = jwtProvider.getRoleFromToken(token);
                
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(() -> role) // Create a simple GrantedAuthority based on the role
                );
                accessor.setUser(authentication); // This sets Principal on the STOMP session (persisted for all subsequent frames)
                log.info("User authenticated and set in accessor: {}", userId);

                log.info("WebSocket CONNECT received, user authenticated: {}, role: {}", userId, role);
                return message;
            }
            log.warn("Rejected WebSocket CONNECT: missing or invalid JWT token");
            throw new MessageDeliveryException("WebSocket authentication failed");
        }  
        return message;
    }
}
