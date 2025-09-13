package com.cloud_ide.file_service.controller;

import com.cloud_ide.file_service.service.impl.FolderStructureImpl;
import com.cloud_ide.file_service.service.impl.ProjectBootstrapImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProjectBootstrap {

    private final ProjectBootstrapImpl projectBootstrap;
    private final FolderStructureImpl folderStructure;

    @PostMapping("/init/{projectId}")
    public String initProject(@PathVariable UUID projectId){
        projectBootstrap.initializeProjectStructure(projectId);
        return "Init success";
    }

    @GetMapping("/folder-structure/{projectId}")
    public ResponseEntity<Map<String, Object>> getFolderStructure(@PathVariable("projectId") UUID projectId) {
        Map<String, Object> structure = folderStructure.getProjectFolderStructure(projectId);
        return ResponseEntity.ok(structure);
    }

}
