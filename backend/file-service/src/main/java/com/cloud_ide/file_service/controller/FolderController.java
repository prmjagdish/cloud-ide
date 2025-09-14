package com.cloud_ide.file_service.controller;
import com.cloud_ide.file_service.dto.ApiResponse;
import com.cloud_ide.file_service.service.impl.FolderServiceImpl;
import com.cloud_ide.file_service.service.impl.FolderStructureImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/p/{projectId}/folder")
public class FolderController {

    private final FolderServiceImpl folderService;
    private final FolderStructureImpl folderStructure;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createFolder(
            @PathVariable UUID projectId,
            @RequestParam String folderPath) {
        folderService.createFolder(projectId, folderPath);
        return ResponseEntity.ok(
                new ApiResponse("success", "Folder created successfully", Instant.now())
        );
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteFolder(
            @PathVariable UUID projectId,
            @RequestParam String folderPath) {
        folderService.deleteFolder(projectId, folderPath);
        return ResponseEntity.ok(
                new ApiResponse("success", "Folder deleted successfully", Instant.now())
        );
    }

    @PutMapping("/rename")
    public ResponseEntity<ApiResponse> renameFolder(
            @PathVariable UUID projectId,
            @RequestParam String oldFolderPath,
            @RequestParam String newFolderPath) {
        folderService.renameFolder(projectId, oldFolderPath, newFolderPath);
        return ResponseEntity.ok(
                new ApiResponse("success", "Folder renamed successfully", Instant.now())
        );
    }

    @GetMapping("/structure")
    public ResponseEntity<Map<String, Object>> getFolderStructure(@PathVariable("projectId") UUID projectId) {
        Map<String, Object> structure = folderStructure.getProjectFolderStructure(projectId);
        return ResponseEntity.ok(structure);
    }

    @GetMapping("/list")
    public ResponseEntity<?> listFolderContent(
            @PathVariable UUID projectId,
            @RequestParam String folderPath) {
        List<String> contents = folderService.listFolderContent(projectId, folderPath);
        return ResponseEntity.ok(contents);
    }
}
