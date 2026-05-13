// src/main/java/com/example/IRMS/config/websocket/WebSocketTicketService.java
package com.example.IRMS.config.websocket;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketTicketService {
  // In-memory cache to hold tickets.
  private final Map<String, String> ticketCache = new ConcurrentHashMap<>();

  public String generateTicket(String jwtToken) {
    String ticket = UUID.randomUUID().toString();
    ticketCache.put(ticket, jwtToken);
    return ticket;
  }

  public String consumeTicket(String ticket) {
    // .remove() ensures the ticket is destroyed immediately and can only be used ONCE!
    return ticketCache.remove(ticket);
  }
}