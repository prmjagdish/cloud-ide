package com.cloud_ide.executor_service.websocket;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    private MessageType type;
    private String content;
    private TerminalState terminalState;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    // Additional metadata
    private String userId;
    private String projectId;
    private Object metadata;

    // Constructor for simple log messages
    public WebSocketMessage(MessageType type, String content, String userId, String projectId) {
        this.type = type;
        this.content = content;
        this.userId = userId;
        this.projectId = projectId;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor for state change messages
    public WebSocketMessage(MessageType type, TerminalState terminalState, String userId, String projectId) {
        this.type = type;
        this.terminalState = terminalState;
        this.userId = userId;
        this.projectId = projectId;
        this.timestamp = LocalDateTime.now();
        this.content = "Terminal state changed to: " + terminalState;
    }

    // Static factory methods for common message types
    public static WebSocketMessage log(String content, String userId, String projectId) {
        return new WebSocketMessage(MessageType.LOG, content, userId, projectId);
    }

    public static WebSocketMessage error(String content, String userId, String projectId) {
        return new WebSocketMessage(MessageType.ERROR, content, userId, projectId);
    }

    public static WebSocketMessage status(String content, String userId, String projectId) {
        return new WebSocketMessage(MessageType.STATUS, content, userId, projectId);
    }

    public static WebSocketMessage terminalState(TerminalState state, String userId, String projectId) {
        return new WebSocketMessage(MessageType.TERMINAL_STATE, state, userId, projectId);
    }
}