package com.cloud_ide.file_service.service.impl;

import com.cloud_ide.file_service.service.ProjectBootstrap;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class ProjectBootstrapImpl implements ProjectBootstrap {
    @Value("${minio.bucket}")
    private String bucketName;
    private final MinioClient minioClient;


    @Override
    public void initializeProjectStructure(UUID projectId) {
        Path projectRoot = Paths.get(bucketName + projectId.toString());

        try {
            Files.createDirectories(projectRoot);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create local project directory: " + projectRoot, e);
        }

        // Download template from MinIO
        try (InputStream templateStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object("HelloWorld.zip")
                        .build()
        )) {
            unzip(templateStream, projectRoot);
        } catch (ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException |
                 ServerException | XmlParserException | IOException e) {
            throw new RuntimeException("Failed to download or unzip HelloWorld template from MinIO", e);
        }

        // Upload unzipped files back to MinIO under project folder
        try {
            Files.walk(projectRoot)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        String objectName = projectId + "/" + projectRoot.relativize(path).toString().replace("\\", "/");
                        try (InputStream is = Files.newInputStream(path)) {
                            minioClient.putObject(
                                    PutObjectArgs.builder()
                                            .bucket(bucketName)
                                            .object(objectName)
                                            .stream(is, Files.size(path), -1)
                                            .build()
                            );
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to upload file to MinIO: " + objectName, e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Failed to traverse local project directory for upload", e);
        }
    }

    private void unzip(InputStream inputStream, Path destDir) {
        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path filePath = destDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to unzip project template", e);
        }
    }

}
