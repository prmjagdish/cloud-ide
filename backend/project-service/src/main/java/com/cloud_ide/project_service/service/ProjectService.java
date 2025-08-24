package com.cloud_ide.project_service.service;

import com.cloud_ide.project_service.dto.ProjectRequest;
import com.cloud_ide.project_service.model.Project;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    Project createProject(ProjectRequest request, UUID ownerId);
    Project getProject(UUID projectId, UUID ownerId);
    List<Project> getProjectsByUser(UUID userId);
    Project updateProject(UUID projectId, UUID userId, ProjectRequest request);

    void deleteProject(UUID projectId, UUID ownerId);
}

