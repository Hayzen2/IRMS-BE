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
  private final WebSocketTicketService ticketService;
  
  // Intercept incoming WebSocket messages to authenticate users based on JWT tokens
  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
      
      // 1. Grab the ticket from the STOMP header
      String ticket = accessor.getFirstNativeHeader("ticket");

      if (ticket != null) {
        // 2. Consume the ticket to get the real JWT token
        String token = ticketService.consumeTicket(ticket);

        if (token != null && jwtProvider.validateToken(token)) {
          String userId = jwtProvider.getUserIdFromToken(token);
          String role = jwtProvider.getRoleFromToken(token);

          UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
              userId, null, List.of(() -> role)
          );
          accessor.setUser(authentication);
          
          log.info("WebSocket CONNECT successful using Ticket! User: {}", userId);
          return message;
        }
      }

      log.warn("Rejected WebSocket CONNECT: missing or invalid Ticket");
      throw new MessageDeliveryException("WebSocket authentication failed");
    }
    return message;
  }
}
