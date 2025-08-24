package com.cloud_ide.project_service.controller;

import com.cloud_ide.project_service.dto.ProjectRequest;
import com.cloud_ide.project_service.dto.ProjectResponse;
import com.cloud_ide.project_service.model.Project;
import com.cloud_ide.project_service.service.ProjectService;
import com.cloud_ide.project_service.util.MapperUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final MapperUtil mapperUtil;

    public ProjectController(ProjectService projectService, MapperUtil mapperUtil) {
        this.projectService = projectService;
        this.mapperUtil = mapperUtil;
    }

//    /projects
    @PostMapping
    public ProjectResponse createProject(@RequestBody ProjectRequest request,
                                         @RequestHeader("userId") UUID userId) {
        Project project = projectService.createProject(request, userId);
        return mapperUtil.toProjectResponse(project);
    }

    @GetMapping
    public List<ProjectResponse> getUserProjects(@RequestHeader("userId") UUID userId) {
        List<Project> projects = projectService.getProjectsByUser(userId);
        return projects.stream()
                .map(mapperUtil::toProjectResponse)
                .collect(Collectors.toList());
    }

//    /projects/{id}
    @GetMapping("/{id}")
    public ProjectResponse getProject(@PathVariable UUID id,
                                      @RequestHeader("userId") UUID userId) {
        Project project = projectService.getProject(id, userId);
        return mapperUtil.toProjectResponse(project);
    }

    @PutMapping("/{projectId}")
    public ProjectResponse updateProject(@PathVariable UUID projectId,
                                         @RequestBody ProjectRequest request,
                                         @RequestHeader("userId") UUID userId) {
        Project updatedProject = projectService.updateProject(projectId, userId, request);
        return mapperUtil.toProjectResponse(updatedProject);
    }

//    /projects/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable UUID id,
                              @RequestHeader("userId") UUID userId) {
        projectService.deleteProject(id, userId);
    }
}

