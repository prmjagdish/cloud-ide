package com.cloud_ide.file_service.controller;

import com.cloud_ide.file_service.service.impl.ProjectBootstrapImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProjectBootstrap {

    private final ProjectBootstrapImpl projectBootstrap;

    @PostMapping("/init/{projectId}")
    public String initProject(@PathVariable UUID projectId){
        projectBootstrap.initializeProjectStructure(projectId);
        return "Init success";
    }
}
