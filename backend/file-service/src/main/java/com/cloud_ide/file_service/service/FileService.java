package com.cloud_ide.file_service.service;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface FileService {

    void createFile(UUID projectId, String path, InputStream content, long size, String contentType);

    void updateFile(UUID projectId, String path, InputStream content, long size, String contentType);

    void deleteFile(UUID projectId, String path);

    void renameFile(UUID projectId, String oldPath, String newPath);

    List<String> listFiles(UUID projectId);

    InputStream readFile(UUID projectId, String path);
}
