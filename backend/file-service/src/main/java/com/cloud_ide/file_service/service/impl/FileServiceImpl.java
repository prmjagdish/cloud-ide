package com.cloud_ide.file_service.service.impl;

import com.cloud_ide.file_service.exception.FileNotFoundException;
import com.cloud_ide.file_service.exception.FileServiceException;
import com.cloud_ide.file_service.model.FileMetadata;
import com.cloud_ide.file_service.repository.FileMetadataRepository;
import com.cloud_ide.file_service.service.FileService;
import com.cloud_ide.file_service.util.MinioUtil;
import io.minio.*;
import io.minio.errors.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    @Value("${minio.bucket}")
    private String bucketName;

    private final FileMetadataRepository repository;
    private final MinioClient minioClient;
    private final MinioUtil minioUtil;
    private final FolderStructureImpl folderStructure;



    @Override
    @Transactional
    public void createFile(UUID projectId, String path, InputStream content, long size, String contentType) {
        try {
            if (projectId == null || path == null || path.isBlank()) {
                throw new FileServiceException("ProjectId and path must not be null/empty");
            }

            if (repository.existsByProjectIdAndPath(projectId, path)) {
                throw new FileServiceException("File already exists: " + path);
            }

            String objectName = projectId + "/" + path ;

            // Default empty content handling
            if (content == null || size == 0) {
                content = new ByteArrayInputStream(new byte[0]);
                size = 0;
            }
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }

            // Upload file to MinIO

            minioUtil.uploadFile(objectName,content,size,contentType);

            // Save metadata in DB
            FileMetadata metadata = new FileMetadata();
            metadata.setProjectId(projectId);
            metadata.setPath(path);
            metadata.setIsFolder(false);
            metadata.setCreatedAt(Instant.now());
            metadata.setUpdatedAt(Instant.now());
            repository.save(metadata);
            folderStructure.syncWithMinIO(projectId);

        } catch (Exception e) {
            throw new FileServiceException("Failed to create file: " + e.getMessage(), e);
        }
    }


    @Override
    public void updateFile(UUID projectId, String path, InputStream content, long size, String contentType) {
        try {
            String objectName = projectId + "/" + path;

            if (!repository.existsByProjectIdAndPath(projectId, path)) {
                throw new FileNotFoundException("File not found: " + path);
            }

            // Replace file in MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(content, size, -1)
                            .contentType(contentType)
                            .build()
            );

            // Update metadata
            FileMetadata metadata = repository.findByProjectIdAndPath(projectId, path)
                    .orElseThrow(() -> new FileNotFoundException("Metadata not found for: " + path));

            metadata.setUpdatedAt(Instant.now());
            repository.save(metadata);

        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to update file: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public void deleteFile(UUID projectId, String path) {
        try {
            String objectName = projectId + "/" + path;

            if (!repository.existsByProjectIdAndPath(projectId, path)) {
                throw new FileNotFoundException("File not found: " + path);
            }

            // Remove from MinIO
         minioUtil.deleteFile(objectName);
            folderStructure.syncWithMinIO(projectId);

            // Remove metadata
            repository.deleteByProjectIdAndPath(projectId, path);


        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to delete file: " + e.getMessage()
            );
        }
    }

    @Override
    public void renameFile(UUID projectId, String oldPath, String newPath) {
        try {
            if (!repository.existsByProjectIdAndPath(projectId, oldPath)) {
                throw new FileNotFoundException("File not found: " + oldPath);
            }

            String oldObject = projectId + "/" + oldPath;
            String newObject = projectId + "/" + newPath;

            // Copy in MinIO
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(newObject)
                            .source(CopySource.builder()
                                    .bucket(bucketName)
                                    .object(oldObject)
                                    .build())
                            .build()
            );

            // Delete old file in MinIO
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(oldObject)
                            .build()
            );

            // Update metadata
            FileMetadata metadata = repository.findByProjectIdAndPath(projectId, oldPath)
                    .orElseThrow(() -> new FileNotFoundException("Metadata not found for: " + oldPath));

            metadata.setPath(newPath);
            metadata.setUpdatedAt(Instant.now());
            repository.save(metadata);
            folderStructure.syncWithMinIO(projectId);

        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to rename file: " + e.getMessage()
            );
        }
    }

    @Override
    public List<String> listFiles(UUID projectId) {
        try {
            return repository.findAllByProjectId(projectId)
                    .stream()
                    .filter(meta -> !meta.getIsFolder())
                    .map(FileMetadata::getPath)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to list files: " + e.getMessage()
            );
        }
    }

    @Override
    public GetObjectResponse readFile(UUID projectId, String path) {
        try {
            if (!repository.existsByProjectIdAndPath(projectId, path)) {
                throw new FileNotFoundException("File not found: " + path);
            }

            String objectName = projectId + "/" + path;

            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to read file: " + e.getMessage()
            );
        }
    }
}
