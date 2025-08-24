package com.cloud_ide.project_service.client;

import com.cloud_ide.project_service.dto.FileBootstrapRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
//@Slf4j
public class FileServiceFallback implements FileServiceClient {

    @Override
    public void bootstrapProject(FileBootstrapRequest request) {
//        log.error("File Service unavailable for bootstrapProject: {} ", request);
        throw new RuntimeException("File Service is unavailable. Try again later.");
    }

    @Override
    public void deleteProjectFiles(UUID projectId) {
//        log.error("File Service unavailable for deleteProjectFiles: {} ", projectId);
        // Optionally ignore to allow project deletion in DB
    }
}
