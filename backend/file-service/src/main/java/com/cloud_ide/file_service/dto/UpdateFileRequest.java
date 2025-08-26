package com.cloud_ide.file_service.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateFileRequest {
    private String path;
    private MultipartFile file;
}
