package com.cloud_ide.executor_service.websocket;

import com.cloud_ide.executor_service.session.SessionRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketConnectionManager {

    private final SessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    // Map: userId:projectId -> WebSocketSession
    private final Map<String, WebSocketSession> connections = new ConcurrentHashMap<>();

    // Map: WebSocketSession -> userId:projectId (reverse lookup)
    private final Map<WebSocketSession, String> sessionToKey = new ConcurrentHashMap<>();

    /**
     * Register a new WebSocket connection
     */
    public void addConnection(String userId, String projectId, WebSocketSession session) {
        String key = getSessionKey(userId, projectId);

        // Close existing connection if any
        WebSocketSession existingSession = connections.get(key);
        if (existingSession != null && existingSession.isOpen()) {
            try {
                existingSession.close(CloseStatus.NORMAL.withReason("New connection established"));
            } catch (IOException e) {
                log.warn("Failed to close existing WebSocket session for {}", key);
            }
        }

        connections.put(key, session);
        sessionToKey.put(session, key);

        // Refresh session activity
        sessionRegistry.refreshSession(userId, projectId);

        log.info("🔗 WebSocket connected for {}:{}", userId, projectId);

        // Send initial terminal state
        sendInitialState(userId, projectId);
    }

    /**
     * Remove WebSocket connection
     */
    public void removeConnection(WebSocketSession session) {
        String key = sessionToKey.remove(session);
        if (key != null) {
            connections.remove(key);
            log.info("❌ WebSocket disconnected for {}", key);
        }
    }

    /**
     * Send message to specific user session
     */
    public void sendToSession(String userId, String projectId, WebSocketMessage message) {
        String key = getSessionKey(userId, projectId);
        WebSocketSession session = connections.get(key);

        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));

                // Refresh session activity on message send
                sessionRegistry.refreshSession(userId, projectId);

            } catch (IOException e) {
                log.error("❌ Failed to send WebSocket message to {}: {}", key, e.getMessage());
                // Remove broken connection
                removeConnection(session);
            }
        } else {
            log.debug("No active WebSocket connection for {}", key);
        }
    }

    /**
     * Send log message
     */
    public void sendLog(String userId, String projectId, String logContent) {
        sendToSession(userId, projectId, WebSocketMessage.log(logContent, userId, projectId));
    }

    /**
     * Send error message
     */
    public void sendError(String userId, String projectId, String errorContent) {
        sendToSession(userId, projectId, WebSocketMessage.error(errorContent, userId, projectId));
    }

    /**
     * Send status message
     */
    public void sendStatus(String userId, String projectId, String statusContent) {
        sendToSession(userId, projectId, WebSocketMessage.status(statusContent, userId, projectId));
    }

    /**
     * Send terminal state change
     */
    public void sendTerminalState(String userId, String projectId, TerminalState state) {
        sendToSession(userId, projectId, WebSocketMessage.terminalState(state, userId, projectId));
    }

    /**
     * Check if user has active WebSocket connection
     */
    public boolean hasConnection(String userId, String projectId) {
        String key = getSessionKey(userId, projectId);
        WebSocketSession session = connections.get(key);
        return session != null && session.isOpen();
    }

    /**
     * Get active connection count
     */
    public int getConnectionCount() {
        return connections.size();
    }

    /**
     * Send initial state when WebSocket connects
     */
    private void sendInitialState(String userId, String projectId) {
        // Determine current terminal state based on session
        TerminalState currentState = getCurrentTerminalState(userId, projectId);
        sendTerminalState(userId, projectId, currentState);

        // Send welcome message
        sendStatus(userId, projectId, "🔗 Terminal connected - Ready for operations");
    }

    /**
     * Determine current terminal state from session
     */
    private TerminalState getCurrentTerminalState(String userId, String projectId) {
        return sessionRegistry.getSession(userId, projectId)
                .map(session -> {
                    switch (session.getState()) {
                        case PROJECT_BUILDING:
                            return TerminalState.PROJECT_BUILDING;
                        case PROJECT_RUNNING:
                            return TerminalState.PROJECT_RUNNING;
                        case CONTAINER_STARTED:
                        case IDLE:
                            return TerminalState.IDLE;
                        default:
                            return TerminalState.IDLE;
                    }
                })
                .orElse(TerminalState.IDLE);
    }

    /**
     * Generate session key
     */
    private String getSessionKey(String userId, String projectId) {
        return userId + ":" + projectId;
    }
}