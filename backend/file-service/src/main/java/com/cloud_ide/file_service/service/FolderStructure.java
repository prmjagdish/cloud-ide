package com.cloud_ide.file_service.service;

import java.util.Map;
import java.util.UUID;

public interface FolderStructure {
    /**
     * Get folder structure as tree from MinIO (helloworld contents as root)
     */
    Map<String, Object> getProjectFolderStructure(UUID projectId);

    /**
     * Sync database with MinIO after file operations
     */
    void syncWithMinIO(UUID projectId);
}
