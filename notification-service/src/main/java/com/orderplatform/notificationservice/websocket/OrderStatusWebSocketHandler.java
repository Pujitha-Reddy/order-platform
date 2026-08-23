package com.orderplatform.notificationservice.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderplatform.notificationservice.model.OrderStatusUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper redisObjectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("Dashboard client connected: {} (total connected: {})", session.getId(), sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("Dashboard client disconnected: {} (total connected: {})", session.getId(), sessions.size());
    }

    public void broadcast(OrderStatusUpdate update) {
        try {
            String json = redisObjectMapper.writeValueAsString(update);
            TextMessage message = new TextMessage(json);

            sessions.values().forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(message);
                    }
                } catch (IOException e) {
                    log.warn("Failed to send update to session {}: {}", session.getId(), e.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Failed to serialize order status update for broadcast", e);
        }
    }
}