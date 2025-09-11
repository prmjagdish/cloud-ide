package com.cloud_ide.executor_service.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.WebSocketMessage;

import java.net.URI;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TerminalWebSocketHandler implements WebSocketHandler {

    private final WebSocketConnectionManager connectionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Extract userId and projectId from query parameters
        String userId = extractQueryParam(session, "userId");
        String projectId = extractQueryParam(session, "projectId");

        if (userId == null || projectId == null) {
            log.error("❌ WebSocket connection missing userId or projectId parameters");
            session.close(CloseStatus.BAD_DATA.withReason("Missing userId or projectId"));
            return;
        }

        // Register connection
        connectionManager.addConnection(userId, projectId, session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // For now, we only handle incoming keepalive or future command messages
        log.debug("📨 Received WebSocket message: {}", message.getPayload());

        // Future: Handle terminal commands here
        // if (message instanceof TextMessage) {
        //     String payload = ((TextMessage) message).getPayload();
        //     // Parse and handle terminal commands
        // }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("❌ WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        connectionManager.removeConnection(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("🔌 WebSocket connection closed for session {} with status: {}",
                session.getId(), closeStatus);
        connectionManager.removeConnection(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * Extract query parameter from WebSocket URI
     */
    private String extractQueryParam(WebSocketSession session, String paramName) {
        try {
            URI uri = session.getUri();
            if (uri != null && uri.getQuery() != null) {
                String query = uri.getQuery();
                for (String param : query.split("&")) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && keyValue[0].equals(paramName)) {
                        return keyValue[1];
                    }
                }
            }
        } catch (Exception e) {
            log.error("❌ Error extracting query parameter {}: {}", paramName, e.getMessage());
        }
        return null;
    }
}