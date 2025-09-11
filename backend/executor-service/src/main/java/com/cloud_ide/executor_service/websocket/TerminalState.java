package com.cloud_ide.executor_service.websocket;

public enum TerminalState {
    IDLE,               // Container ready, no active process
    PROJECT_BUILDING,   // Project is being built
    PROJECT_RUNNING,    // Project is running

    // Future states for terminal commands
    COMMAND_MODE,       // Terminal ready for commands (future)
    COMMAND_EXECUTING,  // Command is executing (future)

    ERROR,              // Terminal in error state
    DISCONNECTED        // WebSocket disconnected
}