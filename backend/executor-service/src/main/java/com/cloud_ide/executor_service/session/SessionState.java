package com.cloud_ide.executor_service.session;

public enum SessionState {
    CREATED,           // Session created but container not started
    CONTAINER_STARTED, // Container is running but no project activity
    PROJECT_BUILDING,  // Project is being built
    PROJECT_RUNNING,   // Project is actively running
    IDLE,             // Container running but no active project
    STOPPED           // Session ended, container stopped
}