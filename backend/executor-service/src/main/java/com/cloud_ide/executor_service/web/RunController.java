package com.cloud_ide.executor_service.web;

import com.cloud_ide.executor_service.session.IDEService;
import com.cloud_ide.executor_service.session.SessionRegistry;
import com.cloud_ide.executor_service.session.SessionTimeoutService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/run")
public class RunController {

    // ✅ CLEAN: Only dependency on IDEService layer
    private final IDEService ideService;
    private final SessionRegistry sessionRegistry;
    private final SessionTimeoutService timeoutService;

    public RunController(IDEService ideService,
                         SessionRegistry sessionRegistry,
                         SessionTimeoutService timeoutService) {
        this.ideService = ideService;
        this.sessionRegistry = sessionRegistry;
        this.timeoutService = timeoutService;
    }

    /**
     * ✅ CLEAN: Single call to IDEService - it handles everything
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> runProject(
            @RequestParam("userId") String userId,
            @RequestParam("projectId") String projectId,
            @RequestParam("runtimeName") String runtimeName) {

        // ✅ Refresh session on user action
        sessionRegistry.refreshSession(userId, projectId);

        // ✅ Single call to IDEService - handles all orchestration
        // IDEService -> ContainerManager flow
        Map<String, Object> response = ideService.startProject(userId, projectId, runtimeName);

        return ResponseEntity.ok(response);
    }

    /**
     * ✅ CLEAN: Single call to IDEService
     */
    @PostMapping("/stop")
    public ResponseEntity<String> stopProject(@RequestParam("userId") String userId,
                                              @RequestParam("projectId") String projectId) {

        // ✅ Refresh session on user action
        sessionRegistry.refreshSession(userId, projectId);

        // ✅ Single call to IDEService
        ideService.stopProject(userId, projectId);

        return ResponseEntity.ok("Project stopped!");
    }

    /**
     * ✅ CLEAN: Single call to IDEService
     */
    @PostMapping("/stopContainer")  // ✅ Fixed typo
    public ResponseEntity<String> stopContainer(@RequestParam("userId") String userId,
                                                @RequestParam("projectId") String projectId) {

        // ✅ Single call to IDEService - no session refresh needed as we're stopping
        ideService.stopContainer(userId, projectId);

        return ResponseEntity.ok("Container stopped!");
    }

    /**
     * ✅ Get project status through IDEService
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getProjectStatus(
            @RequestParam("userId") String userId,
            @RequestParam("projectId") String projectId) {

        Map<String, Object> response = ideService.getProjectStatus(userId, projectId);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ Session status endpoint
     */
    @GetMapping("/session/status")
    public ResponseEntity<Map<String, Object>> getSessionStatus(
            @RequestParam("userId") String userId,
            @RequestParam("projectId") String projectId) {

        Map<String, Object> response = new HashMap<>();

        boolean hasSession = sessionRegistry.hasSession(userId, projectId);
        response.put("hasSession", hasSession);

        if (hasSession) {
            sessionRegistry.getSession(userId, projectId).ifPresent(session -> {
                response.put("state", session.getState());
                response.put("createdAt", session.getCreatedAt());
                response.put("lastActivityAt", session.getLastActivityAt());
                response.put("projectStartedAt", session.getProjectStartedAt());
                response.put("assignedPort", session.getAssignedPort());
                response.put("isProjectRunning", session.isProjectRunning());
            });

            SessionTimeoutService.SessionTimeoutStatus timeoutStatus =
                    timeoutService.getTimeoutStatus(userId, projectId);
            response.put("timeoutStatus", timeoutStatus.status);
            response.put("idleTimeoutRemainingMinutes", timeoutStatus.idleTimeoutRemainingMinutes);
            response.put("hardTimeoutRemainingMinutes", timeoutStatus.hardTimeoutRemainingMinutes);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * ✅ Manual session refresh
     */
    @PostMapping("/session/keepalive")
    public ResponseEntity<String> keepSessionAlive(
            @RequestParam("userId") String userId,
            @RequestParam("projectId") String projectId) {

        sessionRegistry.refreshSession(userId, projectId);
        return ResponseEntity.ok("Session refreshed");
    }

    /**
     * ✅ Admin/monitoring endpoint
     */
    @GetMapping("/sessions/all")
    public ResponseEntity<Map<String, Object>> getAllSessions() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalSessions", sessionRegistry.getActiveSessionCount());
        response.put("sessions", sessionRegistry.getAllActiveSessions());
        return ResponseEntity.ok(response);
    }
}