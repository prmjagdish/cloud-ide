package com.cloud_ide.file_service.service;

import java.io.InputStream;
import java.util.List;

public interface FileService {

    void createFile(Long projectId, String path, InputStream content, long size, String contentType);

    void updateFile(Long projectId, String path, InputStream content, long size, String contentType);

    void deleteFile(Long projectId, String path);

    void renameFile(Long projectId, String oldPath, String newPath);

    List<String> listFiles(Long projectId);

    InputStream readFile(Long projectId, String path);
}
