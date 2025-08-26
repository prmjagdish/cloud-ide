package com.cloud_ide.file_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileUploadResponse {
    private String status;  // "success"
    private String path;    // Path of uploaded/updated file
    private String message; // Optional message
}
