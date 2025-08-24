package com.cloud_ide.file_service.repository;

import com.cloud_ide.file_service.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> findByProjectId(Long projectId);
    Optional<FileMetadata> findByProjectIdAndPath(Long projectId, String path);
}
