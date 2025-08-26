package com.cloud_ide.file_service.util;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class MinioUtil {

    @Value("${minio.bucket}")
    private String bucketName;

    private final MinioClient minioClient;


    public void uploadFile(String objectName, InputStream fileStream, long size, String contentType) throws Exception{
        System.out.println("MinioUtil object name:"+objectName);
        System.out.println("MinioUtil Input stream:"+ fileStream);
        System.out.println("MinioUtil size:"+ size);
        System.out.println("MinioUtil content type:"+contentType);
        try {
            ensureBucketExists();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(fileStream, size, -1)
                    .contentType(contentType)
                    .build());
            System.out.println("File uploaded successfully: " + objectName);
        } catch (Exception e) {
            System.err.println("MinIO Upload Failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public InputStream downloadFile(String objectName) throws Exception{
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }

    public void deleteFile(String objectName) throws Exception{
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }

    public void ensureBucketExists() throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }
}
