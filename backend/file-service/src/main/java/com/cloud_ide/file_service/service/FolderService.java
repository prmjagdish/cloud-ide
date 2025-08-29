package com.cloud_ide.file_service.service;

import java.util.List;
import java.util.UUID;

public interface FolderService {
    // folder operation
    void createFolder(UUID projectId, String folderPath);
    void deleteFolder(UUID projectId, String folderPath);
    void renameFolder(UUID projectId, String oldFolderPath, String newFolderPath);
    List<String> listFolderContent(UUID projectId, String folderPath);
}
