package com.cloud_ide.project_service.client;

import com.cloud_ide.project_service.dto.FileBootstrapRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "file-service", fallback = FileServiceFallback.class)
public interface FileServiceClient {

    @PostMapping("/files/bootstrap")
    void bootstrapProject(@RequestBody FileBootstrapRequest request);

    @DeleteMapping("/files/{projectId}")
    void deleteProjectFiles(@PathVariable UUID projectId);
}