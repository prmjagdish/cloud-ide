package com.cloud_ide.project_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ProjectDeleteRequest implements Serializable {
    private UUID projectId;
    private UUID userId;
}
