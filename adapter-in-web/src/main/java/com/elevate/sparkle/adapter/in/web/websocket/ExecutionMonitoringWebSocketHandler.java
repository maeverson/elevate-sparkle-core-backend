package com.elevate.sparkle.adapter.in.web.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for real-time execution monitoring
 * Allows frontend to subscribe to execution updates
 */
@Component
public class ExecutionMonitoringWebSocketHandler extends TextWebSocketHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ExecutionMonitoringWebSocketHandler.class);
    
    // Map: executionId -> Set of WebSocket sessions
    private final Map<UUID, Map<String, WebSocketSession>> executionSubscribers = new ConcurrentHashMap<>();
    
    // Map: sessionId -> executionId (for cleanup)
    private final Map<String, UUID> sessionExecutions = new ConcurrentHashMap<>();
    
    private final ObjectMapper objectMapper;
    
    public ExecutionMonitoringWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket connection established: sessionId={}", session.getId());
        
        // Send welcome message
        Map<String, String> welcome = Map.of(
                "type", "CONNECTED",
                "message", "Connected to execution monitoring",
                "sessionId", session.getId()
        );
        
        sendMessage(session, welcome);
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.debug("Received WebSocket message: sessionId={}, payload={}", session.getId(), payload);
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> msg = objectMapper.readValue(payload, Map.class);
            String action = (String) msg.get("action");
            
            if ("SUBSCRIBE".equals(action)) {
                String executionIdStr = (String) msg.get("executionId");
                UUID executionId = UUID.fromString(executionIdStr);
                
                subscribe(session, executionId);
                
            } else if ("UNSUBSCRIBE".equals(action)) {
                unsubscribe(session);
                
            } else if ("PING".equals(action)) {
                Map<String, String> pong = Map.of("type", "PONG");
                sendMessage(session, pong);
            }
            
        } catch (Exception e) {
            logger.error("Error handling WebSocket message", e);
            Map<String, String> error = Map.of(
                    "type", "ERROR",
                    "message", "Invalid message format: " + e.getMessage()
            );
            sendMessage(session, error);
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("WebSocket connection closed: sessionId={}, status={}", session.getId(), status);
        unsubscribe(session);
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket transport error: sessionId={}", session.getId(), exception);
        unsubscribe(session);
    }
    
    // ========================================================================
    // SUBSCRIPTION MANAGEMENT
    // ========================================================================
    
    private void subscribe(WebSocketSession session, UUID executionId) throws IOException {
        String sessionId = session.getId();
        
        // Unsubscribe from previous execution if any
        unsubscribe(session);
        
        // Subscribe to new execution
        executionSubscribers
                .computeIfAbsent(executionId, k -> new ConcurrentHashMap<>())
                .put(sessionId, session);
        
        sessionExecutions.put(sessionId, executionId);
        
        logger.info("Session subscribed to execution: sessionId={}, executionId={}", sessionId, executionId);
        
        Map<String, String> confirmation = Map.of(
                "type", "SUBSCRIBED",
                "executionId", executionId.toString()
        );
        sendMessage(session, confirmation);
    }
    
    private void unsubscribe(WebSocketSession session) {
        String sessionId = session.getId();
        UUID executionId = sessionExecutions.remove(sessionId);
        
        if (executionId != null) {
            Map<String, WebSocketSession> sessions = executionSubscribers.get(executionId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    executionSubscribers.remove(executionId);
                }
            }
            
            logger.info("Session unsubscribed from execution: sessionId={}, executionId={}", sessionId, executionId);
        }
    }
    
    // ========================================================================
    // BROADCAST TO SUBSCRIBERS
    // ========================================================================
    
    /**
     * Broadcast execution update to all subscribed sessions
     * Called by ExecutionEventListener when events occur
     */
    public void broadcastExecutionUpdate(UUID executionId, Map<String, Object> update) {
        Map<String, WebSocketSession> sessions = executionSubscribers.get(executionId);
        
        if (sessions == null || sessions.isEmpty()) {
            logger.debug("No subscribers for execution: executionId={}", executionId);
            return;
        }
        
        logger.debug("Broadcasting update to {} subscribers: executionId={}", sessions.size(), executionId);
        
        sessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    sendMessage(session, update);
                } else {
                    logger.warn("Session closed, removing: sessionId={}", session.getId());
                    unsubscribe(session);
                }
            } catch (IOException e) {
                logger.error("Failed to send update to session: sessionId={}", session.getId(), e);
            }
        });
    }
    
    private void sendMessage(WebSocketSession session, Object message) throws IOException {
        String json = objectMapper.writeValueAsString(message);
        session.sendMessage(new TextMessage(json));
    }
}
