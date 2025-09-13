package com.cloud_ide.executor_service.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@Component
public class SessionRegistry {

    private final Map<String, Session> activeSessions = new ConcurrentHashMap<>();

    /**
     * Create or get existing session
     */
    public Session createSession(String userId, String projectId, String runtimeName) {
        String sessionKey = userId + ":" + projectId;
        Session session = activeSessions.computeIfAbsent(sessionKey,
                k -> new Session(userId, projectId, runtimeName));

        // Update activity even if session exists
        session.updateActivity();

        log.info("📋 Session created/updated for {}:{}", userId, projectId);
        return session;
    }

    /**
     * Get session by userId and projectId
     */
    public Optional<Session> getSession(String userId, String projectId) {
        String sessionKey = userId + ":" + projectId;
        return Optional.ofNullable(activeSessions.get(sessionKey));
    }

    /**
     * Refresh session activity (called on any user interaction)
     */
    public void refreshSession(String userId, String projectId) {
        getSession(userId, projectId).ifPresentOrElse(
                Session::updateActivity,
                () -> log.warn("⚠️ Tried to refresh non-existent session: {}:{}", userId, projectId)
        );
    }

    /**
     * Mark container as started
     */
    public void containerStarted(String userId, String projectId, int port) {
        getSession(userId, projectId).ifPresentOrElse(session -> {
            session.setState(SessionState.CONTAINER_STARTED);
            session.setAssignedPort(port);
            session.updateActivity();
            log.info("🐳 Container started for {}:{} on port {}", userId, projectId, port);
        }, () -> log.error("❌ Cannot mark container started: session not found {}:{}", userId, projectId));
    }

    /**
     * Mark project as building
     */
    public void projectBuilding(String userId, String projectId) {
        getSession(userId, projectId).ifPresentOrElse(session -> {
            session.setState(SessionState.PROJECT_BUILDING);
            session.updateActivity();
            log.info("🔨 Project building for {}:{}", userId, projectId);
        }, () -> log.error("❌ Cannot mark project building: session not found {}:{}", userId, projectId));
    }

    /**
     * Mark project as running
     */
    public void projectStarted(String userId, String projectId) {
        getSession(userId, projectId).ifPresentOrElse(session -> {
            session.startProject();
            log.info("🚀 Project started for {}:{}", userId, projectId);
        }, () -> log.error("❌ Cannot start project: session not found {}:{}", userId, projectId));
    }

    /**
     * Mark project as stopped but keep container running
     */
    public void projectStopped(String userId, String projectId) {
        getSession(userId, projectId).ifPresentOrElse(session -> {
            session.stopProject();
            log.info("⏹️ Project stopped for {}:{}", userId, projectId);
        }, () -> log.error("❌ Cannot stop project: session not found {}:{}", userId, projectId));
    }

    /**
     * Remove session completely (when container is stopped)
     */
    public void removeSession(String userId, String projectId) {
        String sessionKey = userId + ":" + projectId;
        Session removed = activeSessions.remove(sessionKey);
        if (removed != null) {
            removed.setState(SessionState.STOPPED);
            log.info("🛑 Session removed for {}:{}", userId, projectId);
        }
    }

    /**
     * Get all active sessions (for timeout monitoring)
     */
    public Collection<Session> getAllActiveSessions() {
        return activeSessions.values();
    }

    /**
     * Check if session exists
     */
    public boolean hasSession(String userId, String projectId) {
        return getSession(userId, projectId).isPresent();
    }

    /**
     * Get session count for monitoring
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * 🔧 EDGE CASE FIX: Check if user has any active container
     */
    public boolean hasUserActiveContainer(String userId) {
        return getAllActiveSessions()
                .stream()
                .anyMatch(session -> session.getUserId().equals(userId) &&
                        isActiveContainerState(session.getState()));
    }

    /**
     * 🔧 Helper: Check if session state represents active container
     */
    private boolean isActiveContainerState(SessionState state) {
        return state == SessionState.CONTAINER_STARTED ||
                state == SessionState.PROJECT_RUNNING ||
                state == SessionState.PROJECT_BUILDING ||
                state == SessionState.IDLE;
    }

    /**
     * 🔧 EDGE CASE FIX: Get user's active session
     */
    public Optional<Session> getUserActiveSession(String userId) {
        return getAllActiveSessions()
                .stream()
                .filter(session -> session.getUserId().equals(userId) &&
                        isActiveContainerState(session.getState()))
                .findFirst();
    }

    /**
     * 🔧 EDGE CASE FIX: Validate user project ownership
     */
    public boolean hasValidUserProject(String userId, String projectId) {
        return getSession(userId, projectId)
                .map(session -> session.getUserId().equals(userId))
                .orElse(false);
    }

}