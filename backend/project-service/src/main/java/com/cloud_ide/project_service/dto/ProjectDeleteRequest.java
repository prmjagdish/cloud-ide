package com.cloud_ide.project_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ProjectDeleteRequest {
    private UUID projectId;
    private UUID userId;
}
