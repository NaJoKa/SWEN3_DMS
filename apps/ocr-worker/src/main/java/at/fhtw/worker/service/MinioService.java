package at.fhtw.worker.service;

import at.fhtw.worker.config.MinioConfig;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLOutput;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioConfig minioConfig;

    public void downloadFile(String objectName, String destinationPath) throws IOException {
        try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(minioConfig.getMinioBucket())
                .object(objectName)
                .build());
             FileOutputStream outputStream = new FileOutputStream(destinationPath)) {

            byte[] buffer = new byte[1024];
            int lth;
            while ((lth = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, lth);
            }
        } catch (Exception e) {
            throw new IOException("Error downloading file from MinIO", e);
        }
    }

    // Return an InputStream for in-memory processing
    public InputStream downloadFileStream(String objectName) throws IOException {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getMinioBucket())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new IOException("Error getting object stream from MinIO", e);
        }
    }

    // Return the object size (for size-guard checks)
    public long getObjectSize(String objectName) throws IOException {

        try {
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getMinioBucket())
                    .object(objectName)
                    .build());
            return stat.size();
        } catch (Exception e) {
            throw new IOException("Error getting object metadata from MinIO", e);
        }
    }
}
