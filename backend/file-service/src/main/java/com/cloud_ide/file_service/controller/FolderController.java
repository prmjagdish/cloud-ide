package com.cloud_ide.file_service.controller;

import com.cloud_ide.file_service.dto.ApiResponse;
import com.cloud_ide.file_service.service.FolderService;
import com.cloud_ide.file_service.service.impl.FolderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/folder")
public class FolderController {

    private final FolderServiceImpl folderService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createFolder(
            @PathVariable Long projectId,
            @RequestParam String folderPath) {
        folderService.createFolder(projectId, folderPath);
        return ResponseEntity.ok(
                new ApiResponse("success", "Folder created successfully", Instant.now())
        );
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteFolder(
            @PathVariable Long projectId,
            @RequestParam String folderPath) {
        folderService.deleteFolder(projectId, folderPath);
        return ResponseEntity.ok(
                new ApiResponse("success", "Folder deleted successfully", Instant.now())
        );
    }

    @PutMapping("/rename")
    public ResponseEntity<ApiResponse> renameFolder(
            @PathVariable Long projectId,
            @RequestParam String oldFolderPath,
            @RequestParam String newFolderPath) {
        folderService.renameFolder(projectId, oldFolderPath, newFolderPath);
        return ResponseEntity.ok(
                new ApiResponse("success", "Folder renamed successfully", Instant.now())
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> listFolderContent(
            @PathVariable Long projectId,
            @RequestParam String folderPath) {
        List<String> contents = folderService.listFolderContent(projectId, folderPath);
        return ResponseEntity.ok(contents);
    }
}
