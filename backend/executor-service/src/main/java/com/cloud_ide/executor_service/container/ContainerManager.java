package com.cloud_ide.executor_service.container;

import com.cloud_ide.executor_service.runtime.RuntimeConfig;
import com.cloud_ide.executor_service.runtime.RuntimeRegistry;
import com.cloud_ide.executor_service.session.SessionRegistry;
import com.cloud_ide.executor_service.session.SessionState;
import com.cloud_ide.executor_service.websocket.WebSocketConnectionManager;
import com.cloud_ide.executor_service.websocket.TerminalState;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
@Component
public class ContainerManager {

    private final MinioClient minioClient;
    private final RuntimeRegistry runtimeRegistry;
    private final SessionRegistry sessionRegistry;
    private final WebSocketConnectionManager webSocketManager;

    // Track project ports
    private final Map<String, Integer> projectPortMap = new ConcurrentHashMap<>();

    // 🔧 EDGE CASE FIX: Request deduplication to prevent rapid clicking
    private final Map<String, Long> lastRequestTime = new ConcurrentHashMap<>();
    private final long REQUEST_COOLDOWN_MS = 5000; // 5 seconds

    // 🔧 EDGE CASE FIX: User-level synchronization locks
    private final Map<String, Object> userLocks = new ConcurrentHashMap<>();

    public ContainerManager(MinioClient minioClient,
                            RuntimeRegistry runtimeRegistry,
                            SessionRegistry sessionRegistry,
                            WebSocketConnectionManager webSocketManager) {
        this.minioClient = minioClient;
        this.runtimeRegistry = runtimeRegistry;
        this.sessionRegistry = sessionRegistry;
        this.webSocketManager = webSocketManager;
    }

    // ----------------- Container Lifecycle -----------------

    /**
     * 🔧 ENHANCED: Complete edge case handling for container start
     */
    public void startContainer(String userId, String projectId, String runtimeName) {
        String containerName = projectId; // Keep as projectId since using unique UUIDs

        try {
            // 🔧 EDGE CASE FIX 1: Request deduplication
            if (!validateRequestTiming(userId, projectId)) {
                return;
            }

            // 🔧 EDGE CASE FIX 2: User-level container enforcement with synchronization
            Object userLock = userLocks.computeIfAbsent(userId, k -> new Object());
            synchronized (userLock) {
                // Double-check inside synchronized block
                if (!validateUserContainerPolicy(userId, projectId)) {
                    return;
                }

                // 🔧 EDGE CASE FIX 3: Comprehensive pre-start validation
                if (!validateContainerStart(userId, projectId, runtimeName, containerName)) {
                    return;
                }

                // 🔧 EDGE CASE FIX 4: Atomic container start with rollback
                performContainerStart(userId, projectId, runtimeName, containerName);
            }
        } catch (Exception e) {
            log.error("🚨 Critical error in startContainer for {}:{}", userId, projectId, e);
            rollbackContainerStart(userId, projectId, containerName);
            webSocketManager.sendError(userId, projectId,
                    "❌ Container start failed: " + e.getMessage());
        } finally {
            // Cleanup user lock if no active sessions
            cleanupUserLock(userId);
        }
    }

    /**
     * 🔧 EDGE CASE FIX: Prevent rapid successive requests
     */
    private boolean validateRequestTiming(String userId, String projectId) {
        String userKey = userId;
        long currentTime = System.currentTimeMillis();

        Long lastTime = lastRequestTime.get(userKey);
        if (lastTime != null && (currentTime - lastTime) < REQUEST_COOLDOWN_MS) {
            String errorMsg = "⏳ Please wait " + ((REQUEST_COOLDOWN_MS - (currentTime - lastTime)) / 1000) +
                    " seconds before starting another container";
            webSocketManager.sendError(userId, projectId, errorMsg);
            log.warn("🚫 Rate limited request from user {}", userId);
            return false;
        }

        lastRequestTime.put(userKey, currentTime);
        return true;
    }

    /**
     * 🔧 EDGE CASE FIX: Enforce one container per user policy
     */
    private boolean validateUserContainerPolicy(String userId, String projectId) {
        // Check if user already has an active container
        boolean hasActiveContainer = sessionRegistry.getAllActiveSessions()
                .stream()
                .anyMatch(session -> session.getUserId().equals(userId) &&
                        isActiveContainerState(session.getState()));

        if (hasActiveContainer) {
            String errorMsg = "🚫 You already have an active container running. Please stop it first.";
            webSocketManager.sendError(userId, projectId, errorMsg);
            log.warn("🚫 User {} attempted to start multiple containers", userId);
            return false;
        }

        return true;
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
     * 🔧 EDGE CASE FIX: Comprehensive pre-start validation
     */
    private boolean validateContainerStart(String userId, String projectId, String runtimeName, String containerName) {
        // 1. Validate runtime exists
        RuntimeConfig config = runtimeRegistry.getRuntime(runtimeName);
        if (config == null) {
            webSocketManager.sendError(userId, projectId, "❌ Runtime not found: " + runtimeName);
            return false;
        }

        // 2. Check if container already exists (cleanup orphaned containers)
        if (isContainerExists(containerName)) {
            log.warn("🧹 Cleaning up existing container: {}", containerName);
            forceRemoveContainer(containerName);
        }

        // 3. Validate session ownership
        if (!sessionRegistry.hasSession(userId, projectId)) {
            webSocketManager.sendError(userId, projectId, "❌ Invalid session");
            return false;
        }

        // 4. Check resource availability (basic check)
        if (!hasAvailableResources()) {
            webSocketManager.sendError(userId, projectId, "❌ System resources unavailable");
            return false;
        }

        return true;
    }

    /**
     * 🔧 EDGE CASE FIX: Atomic container start with comprehensive error handling
     */
    private void performContainerStart(String userId, String projectId, String runtimeName, String containerName) throws Exception {
        RuntimeConfig config = runtimeRegistry.getRuntime(runtimeName);

        // Create project directory
        Path projectPath = Paths.get("/tmp", projectId, "HelloWorld");
        if (!Files.exists(projectPath)) {
            Files.createDirectories(projectPath);
        }

        String dockerPath = toDockerPath(projectPath);
        int hostPort = 9000 + Math.abs(projectId.hashCode() % 1000);

        webSocketManager.sendStatus(userId, projectId, "🐳 Starting container...");

        ProcessBuilder pb = new ProcessBuilder(
                "docker", "run", "-d",

                // — Run as non-root inside container
                "--user", "1001:1001",

                // — Drop all Linux capabilities
                "--cap-drop", "ALL",

                // — Mount rootfs read-only and secure /tmp
                "--read-only",
                "--tmpfs", "/tmp:rw,noexec,nosuid,size=100m",

                // — Resource limits
                "--memory", "512m",
                "--memory-reservation", "256m",
                "--cpus", "1.0",
                "--cpu-shares", "1024",
                "--pids-limit", "256",
                "--ulimit", "nofile=1024:1024",

                // — Security options
                "--security-opt", "no-new-privileges:true",
//                    "--security-opt", "seccomp=unconfined",  // only for linux enviroment
                "--security-opt", "apparmor:docker-default",

                // — Network isolation
//                    "--network", "tenant-" + userId + "-net",

                // — Standard options
                "--name", projectId,
//                    "-e", "SERVER_PORT=8080",
                "-p", hostPort + ":8080",
                "-v", dockerPath + ":" + config.getWorkDir(),
                "-w", config.getWorkDir(),

                // — Base image and command
                config.getBaseImage(),
                "tail", "-f", "/dev/null"
        );

        pb.redirectErrorStream(true);
        Process p = pb.start();

        // 🔧 EDGE CASE FIX: Better error handling for Docker output
        StringBuilder errorOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("[Docker] {}", line);
                errorOutput.append(line).append("\n");
            }
        }

        int exitCode = p.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Docker run failed with exit code " + exitCode +
                    ". Output: " + errorOutput.toString());
        }

        // Verify container is actually running
        if (!isContainerRunning(containerName)) {
            throw new RuntimeException("Container started but not running");
        }

        // 🔧 EDGE CASE FIX: Atomic success - update all state at once
        projectPortMap.put(projectId, hostPort);
        sessionRegistry.containerStarted(userId, projectId, hostPort);

        webSocketManager.sendStatus(userId, projectId,
                "✅ Container started successfully on port " + hostPort);
        webSocketManager.sendTerminalState(userId, projectId, TerminalState.IDLE);

        log.info("✅ Container {} started successfully for user {} on port {}",
                containerName, userId, hostPort);
    }

    /**
     * 🔧 EDGE CASE FIX: Comprehensive rollback procedure
     */
    private void rollbackContainerStart(String userId, String projectId, String containerName) {
        log.warn("🔄 Rolling back container start for {}:{}", userId, projectId);

        try {
            // 1. Force remove container if it exists
            forceRemoveContainer(containerName);

            // 2. Clean up port mapping
            projectPortMap.remove(projectId);

            // 3. Reset session state
            sessionRegistry.getSession(userId, projectId).ifPresent(session -> {
                session.setState(SessionState.CREATED);
                session.setAssignedPort(-1);
            });

            // 4. Clean up project files
            Path projectPath = Paths.get("/tmp", projectId);
            if (Files.exists(projectPath)) {
                deleteDirectory(projectPath);
            }

            // 5. Clean up request timing
            lastRequestTime.remove(userId);

            log.info("🔄 Rollback completed for {}:{}", userId, projectId);

        } catch (Exception rollbackException) {
            log.error("🚨 CRITICAL: Rollback failed for {}:{}", userId, projectId, rollbackException);
        }
    }

    /**
     * 🔧 EDGE CASE FIX: Enhanced container existence check
     */
    private boolean isContainerExists(String containerName) {
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "inspect", containerName);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 🔧 EDGE CASE FIX: Force remove container with comprehensive cleanup
     */
    private void forceRemoveContainer(String containerName) {
        try {
            // Stop first, then remove
            ProcessBuilder stopPb = new ProcessBuilder("docker", "stop", containerName);
            stopPb.redirectErrorStream(true);
            stopPb.start().waitFor();

            ProcessBuilder rmPb = new ProcessBuilder("docker", "rm", "-f", containerName);
            rmPb.redirectErrorStream(true);
            rmPb.start().waitFor();

            log.info("🧹 Force removed container: {}", containerName);
        } catch (Exception e) {
            log.warn("⚠️ Failed to force remove container {}: {}", containerName, e.getMessage());
        }
    }

    /**
     * 🔧 EDGE CASE FIX: Basic resource availability check
     */
    private boolean hasAvailableResources() {
        try {
            // Check active container count (basic limit)
            int activeContainers = sessionRegistry.getActiveSessionCount();
            int maxContainers = 10; // Configurable limit

            if (activeContainers >= maxContainers) {
                log.warn("🚫 Resource limit reached: {} active containers", activeContainers);
                return false;
            }

            // TODO: Add memory/CPU checks if needed
            return true;
        } catch (Exception e) {
            log.error("❌ Error checking resource availability", e);
            return false;
        }
    }

    /**
     * 🔧 EDGE CASE FIX: Cleanup user locks when no active sessions
     */
    private void cleanupUserLock(String userId) {
        try {
            boolean hasActiveSessions = sessionRegistry.getAllActiveSessions()
                    .stream()
                    .anyMatch(session -> session.getUserId().equals(userId));

            if (!hasActiveSessions) {
                userLocks.remove(userId);
            }
        } catch (Exception e) {
            log.debug("Minor issue cleaning up user lock for {}: {}", userId, e.getMessage());
        }
    }

    // 🔧 ENHANCED: Container running check with better error handling
    public boolean isContainerRunning(String projectId) {
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "inspect", "-f", "{{.State.Running}}", projectId);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    return false;
                }

                return line != null && line.equalsIgnoreCase("true");
            }
        } catch (IOException | InterruptedException e) {
            log.debug("Error checking container status for {}: {}", projectId, e.getMessage());
            return false;
        }
    }

    // 🔧 ENHANCED: Stop container with better cleanup
    public void stopContainer(String userId, String projectId) {
        try {
            webSocketManager.sendStatus(userId, projectId, "🛑 Stopping container...");

            // Force remove container
            forceRemoveContainer(projectId);

            // Cleanup project folder
            Path projectPath = Paths.get("/tmp", projectId);
            if (Files.exists(projectPath)) {
                deleteDirectory(projectPath);
            }

            // Cleanup mappings
            projectPortMap.remove(projectId);
            lastRequestTime.remove(userId);

            // Remove session completely
            sessionRegistry.removeSession(userId, projectId);

            webSocketManager.sendStatus(userId, projectId, "✅ Container stopped and cleaned up");
            webSocketManager.sendTerminalState(userId, projectId, TerminalState.DISCONNECTED);

            log.info("✅ Container {} stopped and cleaned up", projectId);

        } catch (Exception e) {
            log.error("❌ Failed to stop container {}:{}", userId, projectId, e);
            webSocketManager.sendError(userId, projectId, "❌ Failed to stop container: " + e.getMessage());
        }
    }

    // 🔧 ENHANCED: Build and run with better validation
    public void buildAndRunProject(String userId, String projectId, String runtimeName,
                                   Consumer<String> logConsumer) {
        Path projectPath = Paths.get("/tmp", projectId, "HelloWorld");

        try {
            // 🔧 EDGE CASE FIX: Validate before proceeding
            if (!validateProjectBuild(userId, projectId, runtimeName)) {
                return;
            }

            RuntimeConfig config = runtimeRegistry.getRuntime(runtimeName);

            webSocketManager.sendStatus(userId, projectId, "📁 Fetching project files...");
            fetchProjectFromMinio("userworkspace", projectId, projectPath, logConsumer, userId);

            // 🔧 EDGE CASE FIX: Double-check container is running
            if (!isContainerRunning(projectId)) {
                String errorMsg = "❌ Container not running. Please start container first.";
                logConsumer.accept(errorMsg);
                webSocketManager.sendError(userId, projectId, errorMsg);
                return;
            }

            // Mark project as building
            sessionRegistry.projectBuilding(userId, projectId);
            webSocketManager.sendTerminalState(userId, projectId, TerminalState.PROJECT_BUILDING);
            webSocketManager.sendStatus(userId, projectId, "🔨 Building project...");

            // Build inside container
            executeInContainer(projectId, config.getBuildCommand(), logConsumer, userId);

            // Mark project as running
            sessionRegistry.projectStarted(userId, projectId);
            webSocketManager.sendTerminalState(userId, projectId, TerminalState.PROJECT_RUNNING);
            webSocketManager.sendStatus(userId, projectId, "🚀 Running project...");

            // Run inside container
            executeInContainer(projectId, config.getRunCommand(), logConsumer, userId);

        } catch (Exception e) {
            String errorMsg = "❌ Error in build/run: " + e.getMessage();
            logConsumer.accept(errorMsg);
            webSocketManager.sendError(userId, projectId, errorMsg);

            // 🔧 EDGE CASE FIX: Reset session state on failure
            sessionRegistry.getSession(userId, projectId).ifPresent(session -> {
                session.setState(SessionState.IDLE);
            });
        }
    }

    /**
     * 🔧 EDGE CASE FIX: Validate project build preconditions
     */
    private boolean validateProjectBuild(String userId, String projectId, String runtimeName) {
        // Check runtime exists
        RuntimeConfig config = runtimeRegistry.getRuntime(runtimeName);
        if (config == null) {
            webSocketManager.sendError(userId, projectId, "❌ Runtime not found: " + runtimeName);
            return false;
        }

        // Check session exists and is valid
        if (!sessionRegistry.hasSession(userId, projectId)) {
            webSocketManager.sendError(userId, projectId, "❌ Invalid session for build");
            return false;
        }

        return true;
    }

    // 🔧 ENHANCED: Stop project with better state management
    public void stopProject(String userId, String projectId) {
        try {
            webSocketManager.sendStatus(userId, projectId, "⏹️ Stopping project...");

            // Kill Java processes in container
            ProcessBuilder pb = new ProcessBuilder("docker", "exec", projectId, "pkill", "-f", "java");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.waitFor();

            // Update session state
            sessionRegistry.projectStopped(userId, projectId);

            webSocketManager.sendStatus(userId, projectId, "✅ Project stopped - Terminal ready");
            webSocketManager.sendTerminalState(userId, projectId, TerminalState.IDLE);

            log.info("✅ Project {} stopped inside container", projectId);

        } catch (Exception e) {
            log.error("❌ Failed to stop project {} inside container", projectId, e);
            webSocketManager.sendError(userId, projectId, "❌ Failed to stop project: " + e.getMessage());
        }
    }

    // ----------------- Existing Helper Methods (Enhanced) -----------------

    private void executeInContainer(String projectId, String command, Consumer<String> logConsumer, String userId)
            throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("docker", "exec", projectId);
        pb.command().addAll(Arrays.asList(command.split(" ")));
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Stream output with session refresh
        streamOutput(process.getInputStream(), logConsumer,
                () -> sessionRegistry.refreshSession(userId, projectId), userId, projectId);
        process.waitFor();
    }

    private void fetchProjectFromMinio(String bucketName, String projectId,
                                       Path projectPath, Consumer<String> logConsumer, String userId) throws Exception {
        String projectSubfolder = "HelloWorld";
        String objectPrefix = projectId + "/" + projectSubfolder + "/";

        Iterable<Result<Item>> items = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(objectPrefix)
                        .recursive(true)
                        .build()
        );

        for (Result<Item> result : items) {
            Item item = result.get();
            Path localPath = projectPath.resolve(item.objectName().substring(objectPrefix.length()));
            Files.createDirectories(localPath.getParent());

            try (InputStream is = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName).object(item.objectName()).build())) {
                Files.copy(is, localPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        String successMsg = "✅ Project fetched from MinIO to " + projectPath;
        logConsumer.accept(successMsg);
        webSocketManager.sendStatus(userId, projectId, successMsg);
    }

    private String toDockerPath(Path projectPath) {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return projectPath.toAbsolutePath().toString()
                    .replace("\\", "/")
                    .replaceFirst("([A-Za-z]):", "/$1");
        } else {
            return projectPath.toAbsolutePath().toString();
        }
    }

    // 🔧 ENHANCED: Stream output with better error handling
    private void streamOutput(InputStream inputStream, Consumer<String> logConsumer,
                              Runnable onActivity, String userId, String projectId) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logConsumer.accept(line);
                    webSocketManager.sendLog(userId, projectId, line);
                    if (onActivity != null) onActivity.run();
                }
            } catch (IOException e) {
                log.error("❌ Error streaming output: {}", e.getMessage());
                webSocketManager.sendError(userId, projectId, "❌ Stream error: " + e.getMessage());
            }
        }, "OutputStreamThread-" + projectId).start();
    }

    private void deleteDirectory(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
                for (Path child : ds) {
                    deleteDirectory(child);
                }
            }
        }
        Files.deleteIfExists(path);
    }

    public int getProjectPort(String projectId) {
        return projectPortMap.getOrDefault(projectId, -1);
    }
}
