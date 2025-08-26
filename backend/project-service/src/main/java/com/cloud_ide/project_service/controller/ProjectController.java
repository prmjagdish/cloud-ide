package com.cloud_ide.project_service.controller;

import com.cloud_ide.project_service.dto.*;
import com.cloud_ide.project_service.exception.BadRequestException;
import com.cloud_ide.project_service.model.Project;
import com.cloud_ide.project_service.service.ProjectService;
import com.cloud_ide.project_service.service.TaskService;
import com.cloud_ide.project_service.service.impl.ProjectServiceImpl;
import com.cloud_ide.project_service.util.MapperUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectServiceImpl projectService;
    private final TaskService taskService;
    private final MapperUtil mapperUtil;

    public ProjectController(ProjectServiceImpl projectService, TaskService taskService, MapperUtil mapperUtil) {
        this.projectService = projectService;
        this.taskService = taskService;
        this.mapperUtil = mapperUtil;
    }


    @PostMapping("/bootstrap")
    public ProjectResponse createProject(@RequestBody ProjectRequest request,
                                         @RequestHeader("userId") UUID userId) {
        // 1️⃣ Create DB entry
        Project project = projectService.createProject(request, userId);

        // 2️⃣ Send async message with projectId
        ProjectBootstrapRequest bootstrapRequest =
                new ProjectBootstrapRequest(project.getId(), project.getName(),project.getBuildTool().toString(),project.getLanguage().toString(),project.getOwnerId());
        taskService.bootstrapProject(bootstrapRequest);
        ProjectResponse projectResponse = mapperUtil.toProjectResponse(project);
        return projectResponse;
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

    @PutMapping("/{projectId}/rename")
    public ProjectResponse renameProject(@PathVariable UUID projectId,
                                         @RequestBody String newName,
                                         @RequestHeader("userId") UUID userId) {

        if (newName == null || newName.isBlank()) {
            throw new BadRequestException("Project name cannot be blank");
        }

        // 1️⃣ Rename project in DB
        Project project = projectService.renameProject(projectId, userId, newName);

        // 2️⃣ Send async task/event directly
        taskService.renameProject(new ProjectRenameRequest(projectId, newName,userId));

        return mapperUtil.toProjectResponse(project);
    }



    //    /projects/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable UUID id,
                              @RequestHeader("userId") UUID userId) {
        // 1️⃣ Delete project metadata from DB
        projectService.deleteProject(id, userId);

        // 2️⃣ Send async delete event to File Service
        taskService.deleteProject(new ProjectDeleteRequest(id, userId));
    }

}

