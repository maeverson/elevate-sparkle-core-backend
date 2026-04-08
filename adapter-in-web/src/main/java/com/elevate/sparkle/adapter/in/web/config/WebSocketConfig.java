package com.elevate.sparkle.adapter.in.web.config;

import com.elevate.sparkle.adapter.in.web.websocket.ExecutionMonitoringWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration for real-time execution monitoring
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final ExecutionMonitoringWebSocketHandler executionMonitoringHandler;
    
    public WebSocketConfig(ExecutionMonitoringWebSocketHandler executionMonitoringHandler) {
        this.executionMonitoringHandler = executionMonitoringHandler;
    }
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(executionMonitoringHandler, "/ws/executions")
                .setAllowedOrigins("*"); // TODO: Configure CORS properly for production
    }
}
