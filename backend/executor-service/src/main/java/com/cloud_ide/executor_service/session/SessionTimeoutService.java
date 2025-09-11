package com.cloud_ide.executor_service.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionTimeoutService {

    private final SessionRegistry sessionRegistry;
    private final IDEService ideService;

    // Timeout configurations
    private static final long IDLE_TIMEOUT_MINUTES = 10;
    private static final long HARD_TIMEOUT_MINUTES = 10;

    /**
     * Check for timeouts every 2 minutes
     */
    @Scheduled(fixedRate = 120000) // 2 minutes in milliseconds
    public void checkTimeouts() {
        log.debug("🔍 Checking session timeouts...");

        List<Session> sessions = sessionRegistry.getAllActiveSessions().stream().toList();

        if (sessions.isEmpty()) {
            log.debug("📭 No active sessions to check");
            return;
        }

        log.info("⏰ Checking {} active sessions for timeouts", sessions.size());

        for (Session session : sessions) {
            try {
                checkIdleTimeout(session);
                checkHardTimeout(session);
            } catch (Exception e) {
                log.error("❌ Error checking timeout for session {}:{}",
                        session.getUserId(), session.getProjectId(), e);
            }
        }
    }

    /**
     * Check if container has been idle for too long
     */
    private void checkIdleTimeout(Session session) {
        if (session.getState() == SessionState.STOPPED) {
            return; // Already stopped
        }

        LocalDateTime lastActivity = session.getLastActivityAt();
        long minutesSinceActivity = ChronoUnit.MINUTES.between(lastActivity, LocalDateTime.now());

        if (minutesSinceActivity >= IDLE_TIMEOUT_MINUTES) {
            log.warn("⏰ IDLE TIMEOUT: Session {}:{} idle for {} minutes, stopping container",
                    session.getUserId(), session.getProjectId(), minutesSinceActivity);

            // Stop container asynchronously to avoid blocking scheduler
            CompletableFuture.runAsync(() -> {
                try {
                    ideService.stopContainer(session.getUserId(), session.getProjectId());
                    log.info("🛑 Container stopped due to idle timeout: {}:{}",
                            session.getUserId(), session.getProjectId());
                } catch (Exception e) {
                    log.error("❌ Failed to stop idle container {}:{}",
                            session.getUserId(), session.getProjectId(), e);
                }
            });
        }
    }

    /**
     * Check if project has been running for too long (hard timeout)
     */
    private void checkHardTimeout(Session session) {
        if (!session.isProjectRunning() || !session.hasProjectStarted()) {
            return; // Project not running
        }

        LocalDateTime projectStartTime = session.getProjectStartedAt();
        long minutesRunning = ChronoUnit.MINUTES.between(projectStartTime, LocalDateTime.now());

        if (minutesRunning >= HARD_TIMEOUT_MINUTES) {
            log.warn("⏰ HARD TIMEOUT: Project {}:{} running for {} minutes, force stopping",
                    session.getUserId(), session.getProjectId(), minutesRunning);

            // Force stop project asynchronously
            CompletableFuture.runAsync(() -> {
                try {
                    ideService.stopProject(session.getUserId(), session.getProjectId());
                    log.info("🛑 Project force stopped due to hard timeout: {}:{}",
                            session.getUserId(), session.getProjectId());
                } catch (Exception e) {
                    log.error("❌ Failed to force stop project {}:{}",
                            session.getUserId(), session.getProjectId(), e);
                }
            });
        }
    }

    /**
     * Get timeout status for monitoring/debugging
     */
    public SessionTimeoutStatus getTimeoutStatus(String userId, String projectId) {
        return sessionRegistry.getSession(userId, projectId)
                .map(this::calculateTimeoutStatus)
                .orElse(new SessionTimeoutStatus(false, -1, -1, "Session not found"));
    }

    private SessionTimeoutStatus calculateTimeoutStatus(Session session) {
        LocalDateTime now = LocalDateTime.now();

        // Calculate idle time
        long idleMinutes = ChronoUnit.MINUTES.between(session.getLastActivityAt(), now);
        long idleRemainingMinutes = Math.max(0, IDLE_TIMEOUT_MINUTES - idleMinutes);

        // Calculate hard timeout if project is running
        long hardRemainingMinutes = -1;
        if (session.isProjectRunning() && session.hasProjectStarted()) {
            long runningMinutes = ChronoUnit.MINUTES.between(session.getProjectStartedAt(), now);
            hardRemainingMinutes = Math.max(0, HARD_TIMEOUT_MINUTES - runningMinutes);
        }

        String status = String.format("State: %s, Idle in: %d min, Hard timeout in: %s min",
                session.getState(),
                idleRemainingMinutes,
                hardRemainingMinutes == -1 ? "N/A" : String.valueOf(hardRemainingMinutes));

        return new SessionTimeoutStatus(true, idleRemainingMinutes, hardRemainingMinutes, status);
    }

    /**
     * Status class for timeout information
     */
    public static class SessionTimeoutStatus {
        public final boolean exists;
        public final long idleTimeoutRemainingMinutes;
        public final long hardTimeoutRemainingMinutes;
        public final String status;

        public SessionTimeoutStatus(boolean exists, long idleRemaining, long hardRemaining, String status) {
            this.exists = exists;
            this.idleTimeoutRemainingMinutes = idleRemaining;
            this.hardTimeoutRemainingMinutes = hardRemaining;
            this.status = status;
        }
    }
}