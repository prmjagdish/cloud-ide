package com.cloud_ide.project_service.service.impl;

import com.cloud_ide.project_service.client.FileServiceClient;
import com.cloud_ide.project_service.dto.FileBootstrapRequest;
import com.cloud_ide.project_service.dto.ProjectRequest;
import com.cloud_ide.project_service.exception.BadRequestException;
import com.cloud_ide.project_service.exception.ProjectNotFoundException;
import com.cloud_ide.project_service.exception.ResourceNotFoundException;
import com.cloud_ide.project_service.exception.UnauthorizedException;
import com.cloud_ide.project_service.model.Project;
import com.cloud_ide.project_service.repository.ProjectRepository;
import com.cloud_ide.project_service.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final FileServiceClient fileServiceClient;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              FileServiceClient fileServiceClient) {
        this.projectRepository = projectRepository;
        this.fileServiceClient = fileServiceClient;
    }

    @Override
    @Transactional
    public Project createProject(ProjectRequest request, UUID ownerId) {
        Project project = new Project();
        project.setName(request.getName());
        project.setBuildTool(request.getBuildTool());
        project.setLanguage(request.getLanguage());
        project.setOwnerId(ownerId);
        Project savedProject = projectRepository.save(project);

//        try {
//            FileBootstrapRequest fileRequest = FileBootstrapRequest.builder()
//                    .projectId(savedProject.getId())
//                    .buildTool(request.getBuildTool().name())
//                    .language(request.getLanguage().name())
//                    .build();
//
//            fileServiceClient.bootstrapProject(fileRequest);
//        } catch (Exception e) {
//            projectRepository.delete(savedProject);
//            // rollback if File Service fails
//            throw new RuntimeException("FileService bootstrap failed, rolling back project", e);
//        }
        return savedProject;
    }

    @Override
    public List<Project> getProjectsByUser(UUID userId) {
        return projectRepository.findByOwnerId(userId);
    }

    @Override
    @Transactional
    public Project renameProject(UUID projectId, UUID userId, String newName) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        if (!project.getOwnerId().equals(userId)) {
            throw new UnauthorizedException("You are not allowed to rename this project");
        }

        project.setName(newName);
        project.setUpdatedAt(LocalDateTime.now());

        return projectRepository.save(project);
    }



    @Override
    public Project getProject(UUID projectId, UUID ownerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        if (!project.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedException("You are not allowed to access this project");
        }

        return project;
    }



    @Override
    @Transactional
    public void deleteProject(UUID projectId, UUID ownerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        if (!project.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedException("You are not allowed to delete this project");
        }

        // Step 1: delete from DB
        projectRepository.delete(project);

        // Step 2: try deleting files, but don't rollback DB if it fails
//        try {
//            fileServiceClient.deleteProjectFiles(projectId);
//        } catch (Exception e) {
//            // Log error and maybe publish an event for retry
//            log.error("Failed to delete files for project {}", projectId, e);
//            // optional: throw custom exception if you want client to know partial failure
//        }
    }

}

