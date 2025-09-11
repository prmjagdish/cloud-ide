package com.cloud_ide.executor_service.websocket;

public enum MessageType {
    LOG,            // Build/run output logs
    ERROR,          // Error messages
    STATUS,         // Status updates (container started, project building, etc.)
    TERMINAL_STATE, // Terminal state changes

    // Future: Terminal command support
    COMMAND_INPUT,  // User command input (future)
    COMMAND_OUTPUT, // Command execution output (future)
    KEEPALIVE       // Connection keepalive
}