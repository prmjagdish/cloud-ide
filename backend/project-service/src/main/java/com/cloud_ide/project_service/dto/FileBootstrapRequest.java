package com.cloud_ide.project_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileBootstrapRequest {
    private UUID projectId;
    private String buildTool;
    private String language;
}