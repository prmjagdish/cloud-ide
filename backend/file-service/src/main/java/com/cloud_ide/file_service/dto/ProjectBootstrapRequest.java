package com.cloud_ide.file_service.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class ProjectBootstrapRequest {
    private UUID projectId;
}
