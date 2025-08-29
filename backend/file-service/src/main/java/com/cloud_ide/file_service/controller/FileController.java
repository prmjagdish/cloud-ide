package com.cloud_ide.file_service.controller;

import com.cloud_ide.file_service.dto.ApiResponse;
import com.cloud_ide.file_service.dto.FileUploadRequest;
import com.cloud_ide.file_service.dto.FileUploadResponse;
import com.cloud_ide.file_service.dto.UpdateFileRequest;
import com.cloud_ide.file_service.service.FileService;
import com.cloud_ide.file_service.service.impl.FileServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects/{projectId}/file")
@RequiredArgsConstructor
public class FileController {

    private final FileServiceImpl fileService;

    /** Create / Upload File **/
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadFile(
            @PathVariable UUID projectId,
            @ModelAttribute FileUploadRequest request) throws Exception {

        if (request.getFile() != null && !request.getFile().isEmpty()) {
            String path = request.getPath();
            String filename = request.getFile().getOriginalFilename();
            if (!path.endsWith("/")) path += "/";
            // File content provided
            fileService.createFile(
                    projectId,
                    path + filename,
                    request.getFile().getInputStream(),
                    request.getFile().getSize(),
                    request.getFile().getContentType()
            );
        } else {
            // No file uploaded → create empty file
            fileService.createFile(projectId, request.getPath(), null, 0, null);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new FileUploadResponse("success", request.getPath(), "File created successfully"));
    }


    /** Update File **/
    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> updateFile(
            @PathVariable UUID projectId,
            @ModelAttribute UpdateFileRequest request) throws Exception {

        fileService.updateFile(
                projectId,
                request.getPath(),
                request.getFile().getInputStream(),
                request.getFile().getSize(),
                request.getFile().getContentType()
        );

        return ResponseEntity.ok(
                new FileUploadResponse("success", request.getPath(), "File updated successfully"));
    }

    /** Delete File **/
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteFile(@PathVariable UUID projectId, @RequestParam String path) {
        fileService.deleteFile(projectId, path);
        return ResponseEntity.ok(new ApiResponse("success", "File deleted successfully", Instant.now()));
    }

    /** Rename File **/
    @PostMapping("/rename")
    public ResponseEntity<ApiResponse> renameFile(
            @PathVariable UUID projectId,
            @RequestParam String oldPath,
            @RequestParam String newPath) {

        fileService.renameFile(projectId, oldPath, newPath);
        return ResponseEntity.ok(new ApiResponse("success", "File renamed successfully", Instant.now()));
    }

    /** List Files **/
    @GetMapping("/list")
    public ResponseEntity<List<String>> listFiles(@PathVariable UUID projectId) {
        return ResponseEntity.ok(fileService.listFiles(projectId));
    }

    /** Read File **/
    @GetMapping(value="/read",produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> readFile(@PathVariable UUID projectId, @RequestParam String path) {
        return ResponseEntity.ok(new InputStreamResource(fileService.readFile(projectId, path)));
    }
}
