package com.cloud_ide.file_service.service.impl;
import com.cloud_ide.file_service.exception.FileServiceException;
import com.cloud_ide.file_service.exception.FolderNotFoundException;
import com.cloud_ide.file_service.model.FileMetadata;
import com.cloud_ide.file_service.repository.FileMetadataRepository;
import com.cloud_ide.file_service.service.FolderService;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    @Value("${minio.bucket}")
    private String bucketName;

    private final MinioClient minioClient;
    private final FileMetadataRepository repository;

    @Override
    @Transactional
    public void createFolder(UUID projectId, String folderPath) {
        String fullPath = projectId + "/" + folderPath + "/";
        try {
            // Save empty object in MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fullPath)
                            .stream(new java.io.ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build()
            );

            // Save in DB
            FileMetadata metadata = new FileMetadata();
            metadata.setProjectId(projectId);
            metadata.setPath(fullPath);
            metadata.setIsFolder(true);
            metadata.setCreatedAt(Instant.now());
            metadata.setUpdatedAt(Instant.now());
            repository.save(metadata);

        } catch (Exception e) {
            throw new FileServiceException("Failed to create folder: " + folderPath, e);
        }
    }

    @Override
    @Transactional
    public void deleteFolder(UUID projectId, String folderPath) {
        String prefix = projectId + "/" + folderPath + "/";
        try {
            if (!repository.existsByProjectIdAndPath(projectId, prefix)) {
                throw new FolderNotFoundException(
                        "Folder not found for projectId: " + projectId + " and path: " + folderPath
                );
            }

            // Delete objects from MinIO
            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).recursive(true).build()
            );

            for (Result<Item> item : objects) {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(item.get().objectName())
                                .build()
                );
            }

            // Delete from DB
            repository.deleteByProjectIdAndPathStartingWith(projectId, prefix);

        } catch (FolderNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new FileServiceException("Failed to delete folder: " + folderPath, e);
        }
    }

    @Override
    @Transactional
    public void renameFolder(UUID projectId, String oldFolderPath, String newFolderPath) {
        String oldPrefix = projectId + "/" + oldFolderPath + "/";
        String newPrefix = projectId + "/" + newFolderPath + "/";
        try {
            if (!repository.existsByProjectIdAndPath(projectId, oldPrefix)) {
                throw new FolderNotFoundException(
                        "Folder not found for projectId: " + projectId + " and path: " + oldFolderPath
                );
            }

            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).prefix(oldPrefix).recursive(true).build()
            );

            for (Result<Item> result : objects) {
                String oldObjectName = result.get().objectName();
                String newObjectName = oldObjectName.replace(oldPrefix, newPrefix);

                // Copy to new path
                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(bucketName)
                                .object(newObjectName)
                                .source(CopySource.builder().bucket(bucketName).object(oldObjectName).build())
                                .build()
                );

                // Delete old object
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(oldObjectName).build());
            }

            // Update DB paths
            List<FileMetadata> files = repository.findByProjectIdAndPathStartingWith(projectId, oldPrefix);
            for (FileMetadata file : files) {
                file.setPath(file.getPath().replace(oldPrefix, newPrefix));
                file.setUpdatedAt(Instant.now());
            }
            repository.saveAll(files);

        } catch (FolderNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new FileServiceException("Failed to rename folder from " + oldFolderPath + " to " + newFolderPath, e);
        }
    }

    @Override
    public List<String> listFolderContent(UUID projectId, String folderPath) {
        List<String> contents = new ArrayList<>();
        try {
            String prefix = (projectId.toString().equals(folderPath)) ? folderPath + "/" : projectId + "/" + folderPath + "/";
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .recursive(false)
                    .build()
            );

            for (Result<Item> result : results) {
                contents.add(result.get().objectName());
            }
            return contents;
        } catch (Exception e) {
            throw new FileServiceException("Failed to list contents for folder: " + folderPath, e);
        }
    }
}

