package com.cloud_ide.file_service.repository;
import com.cloud_ide.file_service.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    Optional<FileMetadata> findByProjectIdAndPath(UUID projectId, String path);
    boolean existsByProjectIdAndPath(UUID projectId, String path);
    void deleteByProjectIdAndPathStartingWith(UUID projectId, String pathPrefix);
    List<FileMetadata> findByProjectIdAndPathStartingWith(UUID projectId, String pathPrefix);
    void deleteByProjectIdAndPath(UUID projectId, String path);
    List<FileMetadata> findAllByProjectId(UUID projectId);
}
