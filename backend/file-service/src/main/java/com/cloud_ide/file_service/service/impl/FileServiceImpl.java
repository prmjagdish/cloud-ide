package com.cloud_ide.file_service.service.impl;

import com.cloud_ide.file_service.model.FileMetadata;
import com.cloud_ide.file_service.repository.FileMetadataRepository;
import com.cloud_ide.file_service.service.FileService;
import com.cloud_ide.file_service.util.MinioUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl extends FileService {

    private final MinioUtil minioUtil;

    private final FileMetadataRepository repository;

    public FileServiceImpl(MinioUtil minioUtil, FileMetadataRepository repository){
        this.minioUtil = minioUtil;
        this.repository = repository;
    }

    @Override
    @Transactional
    public void createFile(Long projectId, String path, InputStream content, long size, String contentType) {
        try {
            String objectName = projectId + "/" + path;
            minioUtil.uploadFile(objectName, content, size, contentType);

            FileMetadata metadata = new FileMetadata();
            metadata.setProjectId(projectId);
            metadata.setPath(path);
            metadata.setIsFolder(false);
            metadata.setCreatedAt(Instant.now());
            metadata.setUpdatedAt(Instant.now());
            repository.save(metadata);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create file: " + path, e);
        }
    }

    @Override
    public InputStream readFile(Long projectId, String path) {
        try {
            return minioUtil.downloadFile(projectId + "/" + path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file: " + path, e);
        }
    }

    @Override
    @Transactional
    public void updateFile(Long projectId, String path, InputStream content, long size, String contentType) {
        deleteFile(projectId, path);
        createFile(projectId, path, content, size, contentType);
    }

    @Override
    @Transactional
    public void deleteFile(Long projectId, String path) {
        try {
            String objectName = projectId + "/" + path;
            minioUtil.deleteFile(objectName);
            repository.findByProjectIdAndPath(projectId, path).ifPresent(repository::delete);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + path, e);
        }
    }

    @Override
    @Transactional
    public void renameFile(Long projectId, String oldPath, String newPath) {
        try {
            // Download old file
            InputStream content = readFile(projectId, oldPath);

            // Upload to new path
            createFile(projectId, newPath, content, -1, "text/plain");

            // Delete old file
            deleteFile(projectId, oldPath);

            // Update metadata
            repository.findByProjectIdAndPath(projectId, oldPath).ifPresent(metadata -> {
                metadata.setPath(newPath);
                metadata.setUpdatedAt(Instant.now());
                repository.save(metadata);
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to rename file: " + oldPath + " to " + newPath, e);
        }
    }

    @Override
    public List<String> listFiles(Long projectId) {
        return repository.findByProjectId(projectId)
                .stream()
                .map(FileMetadata::getPath)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createFolder(Long projectId, String folderPath) {
        if (repository.findByProjectIdAndPath(projectId, folderPath).isPresent()) {
            throw new RuntimeException("Folder already exists: " + folderPath);
        }

        FileMetadata folder = new FileMetadata();
        folder.setProjectId(projectId);
        folder.setPath(folderPath.endsWith("/") ? folderPath : folderPath + "/");
        folder.setIsFolder(true);
        folder.setCreatedAt(Instant.now());
        folder.setUpdatedAt(Instant.now());
        repository.save(folder);
    }

    @Override
    @Transactional
    public void deleteFolder(Long projectId, String folderPath) {
        String normalizedPath = folderPath.endsWith("/") ? folderPath : folderPath + "/";

        // Delete all files under folder from MinIO & DB
        repository.findByProjectId(projectId).stream()
                .filter(meta -> meta.getPath().startsWith(normalizedPath))
                .forEach(meta -> {
                    if (!meta.getIsFolder()) {
                        minioUtil.deleteFile(projectId + "/" + meta.getPath());
                    }
                    repository.delete(meta);
                });

        // Delete folder itself
        repository.findByProjectIdAndPath(projectId, normalizedPath)
                .ifPresent(repository::delete);
    }

    @Override
    @Transactional
    public void renameFolder(Long projectId, String oldFolderPath, String newFolderPath) {
        String oldPath = oldFolderPath.endsWith("/") ? oldFolderPath : oldFolderPath + "/";
        String newPath = newFolderPath.endsWith("/") ? newFolderPath : newFolderPath + "/";

        List<FileMetadata> items = repository.findByProjectId(projectId);
        for (FileMetadata meta : items) {
            if (meta.getPath().startsWith(oldPath)) {
                String updatedPath = meta.getPath().replaceFirst(oldPath, newPath);

                if (!meta.getIsFolder()) {
                    // Move file in MinIO
                    InputStream content = minioUtil.downloadFile(projectId + "/" + meta.getPath());
                    minioUtil.uploadFile(projectId + "/" + updatedPath, content, -1, "text/plain");
                    minioUtil.deleteFile(projectId + "/" + meta.getPath());
                }

                meta.setPath(updatedPath);
                meta.setUpdatedAt(Instant.now());
                repository.save(meta);
            }
        }
    }

    @Override
    public List<String> listFolderContent(Long projectId, String folderPath) {
        String normalizedPath = folderPath.endsWith("/") ? folderPath : folderPath + "/";
        return repository.findByProjectId(projectId).stream()
                .filter(meta -> meta.getPath().startsWith(normalizedPath) && !meta.getPath().equals(normalizedPath))
                .map(FileMetadata::getPath)
                .collect(Collectors.toList());
    }
}
