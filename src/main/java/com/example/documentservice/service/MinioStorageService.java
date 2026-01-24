package com.example.documentservice.service;

import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    public MinioStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    private static final Logger logger = LoggerFactory.getLogger(MinioStorageService.class);

    // Bucket beim Start anlegen, falls er noch nicht existiert
    @PostConstruct
    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                logger.info("Created bucket: {}", bucketName);
            }
        } catch (Exception e) {
            logger.error("Error ensuring bucket exists: {}", bucketName, e);
            throw new RuntimeException("Failed to ensure bucket exists", e);
        }
    }

    // Datei hochladen, gibt den verwendeten Object-Key zurück
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        try {
            String objectKey = generateObjectKey(file.getOriginalFilename());

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return objectKey;
        } catch (IOException e) {
            // IOExceptions in RuntimeException wrappen
            //ToDo
            throw new RuntimeException("Fehler beim Lesen des Upload-Files", e);
        } catch (Exception e) {
            logger.error("Error uploading file to MinIO", e);
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }

    public InputStream downloadFile(String objectKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error downloading file from MinIO: {}", objectKey, e);
            throw new RuntimeException("Failed to download file from MinIO", e);
        }
    }

    public void deleteFile(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
            logger.info("Deleted file from MinIO: {}", objectKey);
        } catch (Exception e) {
            logger.error("Error deleting file from MinIO: {}", objectKey, e);
            throw new RuntimeException("Failed to delete file from MinIO", e);
        }
    }

    public StatObjectResponse getObjectMetadata(String objectKey) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error getting object metadata from MinIO: {}", objectKey, e);
            throw new RuntimeException("Failed to get object metadata from MinIO", e);
        }
    }

    private String generateObjectKey(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return uuid + extension;
    }
}
