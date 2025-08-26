package com.cloud_ide.project_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectBootstrapRequest {
    private UUID projectId;
    private String projectName;
    private String buildTool;   // "maven" or "gradle"
    private String language;    // "java" (for now)
    private UUID ownerId;     // user who owns the project
}
