package com.cloud_ide.project_service.dto;

import com.cloud_ide.project_service.model.ProjectStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class ProjectReadyEvent {
    private UUID projectId;
    private ProjectStatus status; // e.g. "READY"
    // getters + setters
}
