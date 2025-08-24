package com.cloud_ide.project_service.repository;

import com.cloud_ide.project_service.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project , UUID> {
    List<Project> findByOwnerId(UUID ownerId);
    Optional<Project> findByIdAndOwnerId(UUID id, UUID ownerId);
}
