package com.cloud_ide.file_service.util;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class MinioUtil {

    @Value("${minio.bucket}")
    private String bucketName;

    private final MinioClient minioClient;

    public MinioUtil(MinioClient minioClient){
        this.minioClient = minioClient;
    }

    public void uploadFile(String objectName, InputStream fileStream, long size, String contentType) throws Exception{
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(fileStream,size, -1)
                .contentType(contentType)
                .build());
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
}
