package com.cloud_ide.file_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class FileUploadRequest {
    private String path;
    private MultipartFile file;
}
