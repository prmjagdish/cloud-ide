package com.cloud_ide.executor_service.session;

import com.cloud_ide.executor_service.container.ContainerManager;
import com.cloud_ide.executor_service.file.FileService;
import com.cloud_ide.executor_service.file.TempCleaner;
import com.cloud_ide.executor_service.runtime.RuntimeConfig;
import com.cloud_ide.executor_service.runtime.RuntimeRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IDEService {

    private final FileService fileService;
    private final ContainerManager containerManager;
    private final TempCleaner tempCleaner;
    private final RuntimeRegistry runtimeRegistry;
    private final SessionRegistry sessionRegistry;

    /**
     * ✅ MAIN METHOD: Start project - handles complete flow
     * RunController -> IDEService -> ContainerManager
     */
    public Map<String, Object> startProject(String userId, String projectId, String runtimeName) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("🚀 Starting project for {}:{} with runtime {}", userId, projectId, runtimeName);

            // 1️⃣ Create/update session first
            sessionRegistry.createSession(userId, projectId, runtimeName);

            // 2️⃣ Validate runtime exists
            RuntimeConfig config = runtimeRegistry.getRuntime(runtimeName);
            if (config == null) {
                log.error("❌ Runtime not found: {}", runtimeName);
                response.put("success", false);
                response.put("error", "Runtime not found: " + runtimeName);
                return response;
            }

            // 3️⃣ Fetch project files first
            log.info("📁 Fetching project files for {}", projectId);
            fileService.fetchProject(projectId);

            // 4️⃣ Start container through ContainerManager
            log.info("🐳 Starting container for {}", projectId);
            containerManager.startContainer(userId, projectId, runtimeName);

            // 5️⃣ Verify container is running
            if (!containerManager.isContainerRunning(projectId)) {
                log.error("❌ Failed to start container for {}", projectId);
                response.put("success", false);
                response.put("error", "Failed to start container");
                return response;
            }

            // 6️⃣ Get assigned port
            int port = containerManager.getProjectPort(projectId);

            // 7️⃣ Start async build and run process
            log.info("🔨 Starting async build and run for {}", projectId);
            new Thread(() -> {
                try {
                    runProjectAsync(userId, projectId, runtimeName);
                } catch (Exception e) {
                    log.error("❌ Error in async project execution for {}:{}", userId, projectId, e);
                }
            }).start();

            // 8️⃣ Return success response
            response.put("success", true);
            response.put("projectId", projectId);
            response.put("port", port);
            response.put("status", "BUILDING");

            log.info("✅ Project start initiated successfully for {}:{} on port {}", userId, projectId, port);

        } catch (Exception e) {
            log.error("❌ Failed to start project for {}:{}", userId, projectId, e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    /**
     * ✅ Private helper: Run project async
     */
    private void runProjectAsync(String userId, String projectId, String runtimeName) throws IOException, InterruptedException {
        log.info("🔄 Running project async for {}:{}", userId, projectId);

        // Delegate to ContainerManager for build and run
        containerManager.buildAndRunProject(
                userId,
                projectId,
                runtimeName,
                line -> {
                    log.info("[{}:{}][BUILD/RUN] {}", userId, projectId, line);
                    // Keep session alive on every log output
                    sessionRegistry.refreshSession(userId, projectId);
                }
        );
    }

    /**
     * ✅ Stop project process (keep container running)
     */
    public void stopProject(String userId, String projectId) {
        log.info("⏹️ Stopping project for {}:{}", userId, projectId);

        try {
            // Delegate to ContainerManager
            containerManager.stopProject(userId, projectId);
            log.info("✅ Project stopped successfully for {}:{}", userId, projectId);

        } catch (Exception e) {
            log.error("❌ Failed to stop project for {}:{}", userId, projectId, e);
        }
    }

    /**
     * ✅ Stop container completely
     */
    public void stopContainer(String userId, String projectId) {
        log.info("🛑 Stopping container for {}:{}", userId, projectId);

        try {
            // 1️⃣ Clean project files first
            tempCleaner.cleanup(projectId);

            // 2️⃣ Stop & remove the container through ContainerManager
            containerManager.stopContainer(userId, projectId);

            log.info("✅ Container stopped successfully for {}:{}", userId, projectId);

        } catch (Exception e) {
            log.error("❌ Failed to stop container for {}:{}", userId, projectId, e);
        }
    }

    /**
     * ✅ Check if project is ready
     */
    public boolean isProjectReady(String userId, String projectId) {
        return sessionRegistry.getSession(userId, projectId)
                .map(session -> containerManager.isContainerRunning(projectId))
                .orElse(false);
    }

    /**
     * ✅ Get project status
     */
    public Map<String, Object> getProjectStatus(String userId, String projectId) {
        Map<String, Object> status = new HashMap<>();

        sessionRegistry.getSession(userId, projectId).ifPresentOrElse(
                session -> {
                    status.put("hasSession", true);
                    status.put("state", session.getState());
                    status.put("containerRunning", containerManager.isContainerRunning(projectId));
                    status.put("assignedPort", session.getAssignedPort());
                    status.put("isProjectRunning", session.isProjectRunning());
                },
                () -> {
                    status.put("hasSession", false);
                    status.put("containerRunning", false);
                }
        );

        return status;
    }
}