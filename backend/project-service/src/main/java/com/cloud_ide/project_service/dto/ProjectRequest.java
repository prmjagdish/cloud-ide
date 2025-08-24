package com.cloud_ide.project_service.dto;

import com.cloud_ide.project_service.model.BuildTool;
import com.cloud_ide.project_service.model.Language;
import lombok.Data;

@Data
public class ProjectRequest {
    private String name;
    private BuildTool buildTool;
    private Language language;
    // getters and setters
}
