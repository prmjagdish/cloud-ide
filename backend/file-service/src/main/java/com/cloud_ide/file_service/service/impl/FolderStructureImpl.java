package com.cloud_ide.file_service.service.impl;

import com.cloud_ide.file_service.model.FileMetadata;
import com.cloud_ide.file_service.repository.FileMetadataRepository;
import com.cloud_ide.file_service.service.FolderStructure;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FolderStructureImpl implements FolderStructure {

    private final MinioClient minioClient;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${minio.bucket}")
    private String bucketName; // userworkspace

    @Override
    public Map<String, Object> getProjectFolderStructure(UUID projectId) {
        try {
            // MinIO path: userworkspace/{projectId}/helloworld/
            String projectPrefix = projectId + "/HelloWorld/";

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(projectPrefix)
                            .recursive(true)
                            .build()
            );

            List<String> allPaths = new ArrayList<>();
            for (Result<Item> result : results) {
                Item item = result.get();
                String fullPath = item.objectName();

                // Remove project prefix to get path relative to helloworld/
                String relativePath = fullPath.substring(projectPrefix.length());

                // Only add non-empty paths (skip HelloWorld folder itself)
                if (!relativePath.isEmpty()) {
                    allPaths.add(relativePath);
                }
            }

            return buildTreeFromPaths(allPaths);

        } catch (Exception e) {
            log.error("Error getting folder structure for project: {}", projectId, e);
            return createEmptyRoot();
        }
    }

    @Override
    @Transactional
    public void syncWithMinIO(UUID projectId) {
        try {
            // MinIO path: userworkspace/{projectId}/helloworld/
            String projectPrefix = projectId + "/HelloWorld/";

            // Clear existing database metadata for this project
            fileMetadataRepository.deleteByProjectIdAndPathStartingWith(projectId, "");

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(projectPrefix)
                            .recursive(true)
                            .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                String fullPath = item.objectName();

                // Get path relative to helloworld/
                String relativePath = fullPath.substring(projectPrefix.length());

                // Only save non-empty paths (skip helloworld folder itself)
                if (!relativePath.isEmpty()) {
                    FileMetadata metadata = new FileMetadata();
                    metadata.setProjectId(projectId);
                    metadata.setPath(relativePath);
                    metadata.setIsFolder(item.isDir());
                    metadata.setCreatedAt(Instant.now());
                    metadata.setUpdatedAt(Instant.now());
                    fileMetadataRepository.save(metadata);
                }
            }

            log.info("Database synced with MinIO for project: {}", projectId);

        } catch (Exception e) {
            log.error("Sync failed for project: {}", projectId, e);
            throw new RuntimeException("Database sync failed", e);
        }
    }

    private Map<String, Object> buildTreeFromPaths(List<String> paths) {
        Map<String, Object> root = createEmptyRoot();
        Map<String, Map<String, Object>> nodeMap = new HashMap<>();
        nodeMap.put("", root);

        Collections.sort(paths);
        for (String path : paths) {
            createNodePath(path, nodeMap);
        }
        return root;
    }

    private void createNodePath(String path, Map<String, Map<String, Object>> nodeMap) {
        String[] parts = path.split("/");
        StringBuilder currentPath = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;

            if (currentPath.length() > 0) currentPath.append("/");
            currentPath.append(part);

            String fullCurrentPath = currentPath.toString();

            if (!nodeMap.containsKey(fullCurrentPath)) {
                Map<String, Object> newNode = new HashMap<>();
                newNode.put("id", String.valueOf(Math.abs(fullCurrentPath.hashCode())));
                newNode.put("name", part);

                boolean isFolder = (i < parts.length - 1) || path.endsWith("/");
                if (isFolder) {
                    newNode.put("children", new ArrayList<>());
                }

                nodeMap.put(fullCurrentPath, newNode);

                String parentPath = getParentPath(fullCurrentPath);
                Map<String, Object> parent = nodeMap.get(parentPath);
                if (parent != null && parent.containsKey("children")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
                    children.add(newNode);
                }
            }
        }
    }

    private Map<String, Object> createEmptyRoot() {
        Map<String, Object> root = new HashMap<>();
        root.put("id", "root");
        root.put("name", "root");
        root.put("children", new ArrayList<>());
        return root;
    }

    private String getParentPath(String path) {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash == -1 ? "" : path.substring(0, lastSlash);
    }
}
