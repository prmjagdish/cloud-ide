package com.cloud_ide.project_service.dto;

import com.cloud_ide.project_service.model.BuildTool;
import com.cloud_ide.project_service.model.Language;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private UUID id;
    private String name;
    private BuildTool buildTool;
    private Language language;
    private List<String> files;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

