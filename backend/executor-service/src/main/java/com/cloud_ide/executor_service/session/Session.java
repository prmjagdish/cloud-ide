package com.cloud_ide.executor_service.session;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    private String userId;
    private String projectId;
    private String runtimeName;
    private SessionState state;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivityAt;
    private LocalDateTime projectStartedAt;
    private int assignedPort;

    public Session(String userId, String projectId, String runtimeName) {
        this.userId = userId;
        this.projectId = projectId;
        this.runtimeName = runtimeName;
        this.state = SessionState.CREATED;
        this.createdAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
        this.assignedPort = -1;
    }

    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    public void startProject() {
        this.projectStartedAt = LocalDateTime.now();
        this.state = SessionState.PROJECT_RUNNING;
        updateActivity();
    }

    public void stopProject() {
        this.projectStartedAt = null;
        this.state = SessionState.IDLE;
        updateActivity();
    }

    public boolean isProjectRunning() {
        return state == SessionState.PROJECT_RUNNING || state == SessionState.PROJECT_BUILDING;
    }

    public boolean hasProjectStarted() {
        return projectStartedAt != null;
    }

    public String getSessionKey() {
        return userId + ":" + projectId;
    }
}