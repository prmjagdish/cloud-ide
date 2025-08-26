package com.cloud_ide.file_service.repository;
import com.cloud_ide.file_service.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    Optional<FileMetadata> findByProjectIdAndPath(Long projectId, String path);
    boolean existsByProjectIdAndPath(Long projectId, String path);
    void deleteByProjectIdAndPathStartingWith(Long projectId, String pathPrefix);
    List<FileMetadata> findByProjectIdAndPathStartingWith(Long projectId, String pathPrefix);
    void deleteByProjectIdAndPath(Long projectId, String path);
    List<FileMetadata> findAllByProjectId(Long projectId);

}
