package com.cloud_ide.file_service.service;

import java.util.UUID;

public interface ProjectBootstrap {
    void initializeProjectStructure(UUID projectId);
}
