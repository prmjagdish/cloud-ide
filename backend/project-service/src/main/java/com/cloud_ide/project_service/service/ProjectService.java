package com.cloud_ide.project_service.service;

import com.cloud_ide.project_service.dto.ProjectReadyEvent;
import com.cloud_ide.project_service.dto.ProjectRequest;
import com.cloud_ide.project_service.model.Project;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    Project createProject(ProjectRequest request, UUID ownerId);
    Project getProject(UUID projectId, UUID ownerId);
    List<Project> getProjectsByUser(UUID userId);
    Project renameProject(UUID projectId, UUID userId, String request);
    void updateStatus(ProjectReadyEvent projectReadyEvent);
    void deleteProject(UUID projectId, UUID ownerId);
}

