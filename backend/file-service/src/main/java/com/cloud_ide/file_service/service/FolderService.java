package com.cloud_ide.file_service.service;

import java.util.List;

public interface FolderService {
    // folder operation
    void createFolder(Long projectId, String folderPath);
    void deleteFolder(Long projectId, String folderPath);
    void renameFolder(Long projectId, String oldFolderPath, String newFolderPath);
    List<String> listFolderContent(Long projectId, String folderPath);
}
