package com.cloud_ide.executor_service.container;

import com.cloud_ide.executor_service.runtime.RuntimeConfig;
import com.cloud_ide.executor_service.runtime.RuntimeRegistry;
import com.cloud_ide.executor_service.session.SessionRegistry;
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
    private final WebSocketConnectionManager webSocketManager; // ✅ Added WebSocket manager

    // Track project ports
    private final Map<String, Integer> projectPortMap = new ConcurrentHashMap<>();

    public ContainerManager(MinioClient minioClient,
                            RuntimeRegistry runtimeRegistry,
                            SessionRegistry sessionRegistry,
                            WebSocketConnectionManager webSocketManager) { // ✅ Inject WebSocket manager
        this.minioClient = minioClient;
        this.runtimeRegistry = runtimeRegistry;
        this.sessionRegistry = sessionRegistry;
        this.webSocketManager = webSocketManager;
    }

    // ----------------- Container Lifecycle -----------------

    public void startContainer(String userId, String projectId, String runtimeName) {
        try {
            RuntimeConfig config = runtimeRegistry.getRuntime(runtimeName);
            if (config == null) {
                log.error("Runtime {} not found", runtimeName);
                // ✅ Send error to WebSocket
                webSocketManager.sendError(userId, projectId, "❌ Runtime not found: " + runtimeName);
                return;
            }

            Path projectPath = Paths.get("/tmp", projectId, "helloworld");
            if (!Files.exists(projectPath)) {
                Files.createDirectories(projectPath);
            }

            String dockerPath = toDockerPath(projectPath);

            // Dynamic port assignment (9000-9999)
            int hostPort = 9000 + Math.abs(projectId.hashCode() % 1000);
            projectPortMap.put(projectId, hostPort);

            // ✅ Send status update
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.error("[Docker] {}", line);
                webSocketManager.sendError(userId, projectId, "[Docker] " + line);
            }
            int exitCode = p.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Docker run exited with code " + exitCode);
            }

            p.waitFor();

            // ✅ Update session state
            sessionRegistry.containerStarted(userId, projectId, hostPort);

            // ✅ Send success status and terminal state
            webSocketManager.sendStatus(userId, projectId,
                    "✅ Container started successfully on port " + hostPort);
            webSocketManager.sendTerminalState(userId, projectId, TerminalState.IDLE);

            log.info("Started container {} for user {} on port {}", projectId, userId, hostPort);

        } catch (Exception e) {
            log.error("Failed to start container {}:{}", userId, projectId, e);
            // ✅ Send error to WebSocket
            webSocketManager.sendError(userId, projectId, "❌ Failed to start container: " + e.getMessage());
        }
    }

    public boolean isContainerRunning(String projectId) {
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "inspect", "-f", "{{.State.Running}}", projectId);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            return line != null && line.equalsIgnoreCase("true");
        } catch (IOException e) {
            return false;
        }
    }

    public void stopContainer(String userId, String projectId) {
        try {
            // ✅ Send status update
            webSocketManager.sendStatus(userId, projectId, "🛑 Stopping container...");

            // Remove container
            ProcessBuilder pb = new ProcessBuilder("docker", "rm", "-f", projectId);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.waitFor();

            // Cleanup project folder
            Path projectPath = Paths.get("/tmp", projectId);
            if (Files.exists(projectPath)) {
                deleteDirectory(projectPath);
            }

            projectPortMap.remove(projectId);

            // ✅ Remove session completely
            sessionRegistry.removeSession(userId, projectId);

            // ✅ Send final status and terminal state
            webSocketManager.sendStatus(userId, projectId, "✅ Container stopped and cleaned up");
            webSocketManager.sendTerminalState(userId, projectId, TerminalState.DISCONNECTED);

            log.info("Stopped container {} and cleaned up files", projectId);

        } catch (Exception e) {
            log.error("Failed to stop container {}:{}", userId, projectId, e);
            // ✅ Send error to WebSocket
            webSocketManager.sendError(userId, projectId, "❌ Failed to stop container: " + e.getMessage());
        }
    }

    // ----------------- Project Lifecycle -----------------

    public void buildAndRunProject(String userId, String projectId, String runtimeName,
                                   Consumer<String> logConsumer) {
        Path projectPath = Paths.get("/tmp", projectId, "helloworld");

        try {
            RuntimeConfig config = runtimeRegistry.getRuntime(runtimeName);
            if (config == null) {
                String errorMsg = "❌ Runtime not found: " + runtimeName;
                logConsumer.accept(errorMsg);
                webSocketManager.sendError(userId, projectId, errorMsg);
                return;
            }

            // ✅ Send status update
            webSocketManager.sendStatus(userId, projectId, "📁 Fetching project files...");

            // Fetch latest project from MinIO
            fetchProjectFromMinio("userworkspace", projectId, projectPath, logConsumer, userId);

            // Check container running
            if (!isContainerRunning(projectId)) {
                String errorMsg = "❌ Container not running. Please start container first.";
                logConsumer.accept(errorMsg);
                webSocketManager.sendError(userId, projectId, errorMsg);
                return;
            }

            // ✅ Mark project as building and send terminal state
            sessionRegistry.projectBuilding(userId, projectId);
            webSocketManager.sendTerminalState(userId, projectId, TerminalState.PROJECT_BUILDING);
            webSocketManager.sendStatus(userId, projectId, "🔨 Building project...");

            // Build inside container
            executeInContainer(projectId, config.getBuildCommand(), logConsumer, userId);

            // ✅ Mark project as running when run command starts
            sessionRegistry.projectStarted(userId, projectId);
            webSocketManager.sendTerminalState(userId, projectId, TerminalState.PROJECT_RUNNING);
            webSocketManager.sendStatus(userId, projectId, "🚀 Running project...");

            // Run inside container
            executeInContainer(projectId, config.getRunCommand(), logConsumer, userId);

        } catch (Exception e) {
            String errorMsg = "❌ Error in build/run: " + e.getMessage();
            logConsumer.accept(errorMsg);
            webSocketManager.sendError(userId, projectId, errorMsg);
            e.printStackTrace();
        }
    }

    public void stopProject(String userId, String projectId) {
        try {
            // ✅ Send status update
            webSocketManager.sendStatus(userId, projectId, "⏹️ Stopping project...");

            ProcessBuilder pb = new ProcessBuilder(
                    "docker", "exec", projectId,
                    "pkill", "-f", "java"
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.waitFor();

            // ✅ Update session state to stopped project but keep container
            sessionRegistry.projectStopped(userId, projectId);

            // ✅ Send status and terminal state update
            webSocketManager.sendStatus(userId, projectId, "✅ Project stopped - Terminal ready");
            webSocketManager.sendTerminalState(userId, projectId, TerminalState.IDLE);

            log.info("Stopped project {} inside container", projectId);
        } catch (Exception e) {
            log.error("Failed to stop project {} inside container", projectId, e);
            webSocketManager.sendError(userId, projectId, "❌ Failed to stop project: " + e.getMessage());
        }
    }

    // ----------------- Helpers -----------------

    private void executeInContainer(String projectId, String command, Consumer<String> logConsumer, String userId) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("docker", "exec", projectId);
        pb.command().addAll(Arrays.asList(command.split(" ")));
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // ✅ Stream output to both logConsumer and WebSocket
        streamOutput(process.getInputStream(), logConsumer, () -> sessionRegistry.refreshSession(userId, projectId), userId, projectId);
        process.waitFor();
    }

    private void fetchProjectFromMinio(String bucketName, String projectId,
                                       Path projectPath, Consumer<String> logConsumer, String userId) throws Exception {
        String projectSubfolder = "helloworld";
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
        // ✅ Send to WebSocket
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

    // ✅ Updated to stream to WebSocket
    private void streamOutput(InputStream inputStream, Consumer<String> logConsumer, Runnable onActivity, String userId, String projectId) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Send to legacy log consumer
                    logConsumer.accept(line);

                    // ✅ Send to WebSocket terminal
                    webSocketManager.sendLog(userId, projectId, line);

                    // Activity callback
                    if (onActivity != null) onActivity.run();
                }
            } catch (IOException e) {
                log.error("Error streaming output: {}", e.getMessage());
                webSocketManager.sendError(userId, projectId, "❌ Stream error: " + e.getMessage());
            }
        }).start();
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

    // Expose project host port
    public int getProjectPort(String projectId) {
        return projectPortMap.getOrDefault(projectId, -1);
    }
}