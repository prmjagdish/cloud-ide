package com.cloud_ide.project_service.util;

import com.cloud_ide.project_service.dto.ProjectRequest;
import com.cloud_ide.project_service.dto.ProjectResponse;
import com.cloud_ide.project_service.model.Project;
import org.springframework.stereotype.Component;

@Component
public class MapperUtil {
    // Convert Project entity → ProjectResponse DTO
    public ProjectResponse toProjectResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .buildTool(project.getBuildTool())
                .language(project.getLanguage())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    // Convert ProjectRequest DTO → Project entity
    public static Project toProjectEntity(ProjectRequest request, Project project) {
        project.setName(request.getName());
        project.setBuildTool(request.getBuildTool());
        project.setLanguage(request.getLanguage());
        return project;
    }
}
