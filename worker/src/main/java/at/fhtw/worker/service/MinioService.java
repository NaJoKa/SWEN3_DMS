package at.fhtw.worker.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    public void downloadFile(String bucketName, String objectName, String destinationPath) throws IOException {
        try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
             FileOutputStream outputStream = new FileOutputStream(new File(destinationPath))) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        } catch (Exception e) {
            throw new IOException("Error downloading file from MinIO", e);
        }
    }
}
