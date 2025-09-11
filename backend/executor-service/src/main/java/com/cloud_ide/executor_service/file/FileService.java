package com.cloud_ide.executor_service.file;

import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class FileService {

    private final MinioClient minioClient;
    private final String bucketName = "userworkspace"; // your bucket

    public FileService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * Downloads the entire 'helloworld' folder inside projectId to /tmp/<projectId>/helloworld
     *
     * @param projectId unique project identifier
     * @return Path to local temp project folder
     */
    public Path fetchProject(String projectId) {
        try {
            Path tmpDir = Paths.get("/tmp", projectId, "helloworld");
            if (!Files.exists(tmpDir)) {
                Files.createDirectories(tmpDir);
            }

            // List all objects under projectId/helloworld/
            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(projectId + "/helloworld/")
                            .recursive(true)
                            .build()
            );

            for (Result<Item> result : objects) {
                Item item = result.get();
                String objectName = item.objectName(); // e.g. projectId/helloworld/src/Main.java
                Path localPath = tmpDir.resolve(objectName.replace(projectId + "/helloworld/", ""));

                // Create parent directories if needed
                if (localPath.getParent() != null && !Files.exists(localPath.getParent())) {
                    Files.createDirectories(localPath.getParent());
                }

                try (InputStream stream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build()
                )) {
                    try (FileOutputStream fos = new FileOutputStream(localPath.toFile())) {
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = stream.read(buffer)) != -1) {
                            fos.write(buffer, 0, read);
                        }
                    }
                }
            }

            log.info("Project {} downloaded to {}", projectId, tmpDir.toString());
            return tmpDir;

        } catch (Exception e) {
            log.error("Failed to fetch project {}: {}", projectId, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
