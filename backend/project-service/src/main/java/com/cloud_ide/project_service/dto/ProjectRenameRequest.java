package com.cloud_ide.project_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ProjectRenameRequest {
    private UUID projectId;
    private String newName;
    private UUID userId;
}
