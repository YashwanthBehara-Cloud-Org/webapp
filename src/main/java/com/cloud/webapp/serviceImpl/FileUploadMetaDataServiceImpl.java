
package com.cloud.webapp.serviceImpl;

import com.cloud.webapp.model.FileUploadMetaData;
import com.cloud.webapp.repository.FileUploadMetaDataRepository;
import com.cloud.webapp.service.FileUploadMetaDataService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileUploadMetaDataServiceImpl implements FileUploadMetaDataService {

    private final S3Client s3Client;
    private final String bucketName;
    private final FileUploadMetaDataRepository fileMetadataRepository;

    public FileUploadMetaDataServiceImpl(@Value("${aws.s3.bucketName}") String bucketName,
                                         FileUploadMetaDataRepository fileMetadataRepository) {
        String awsAccessKeyId = System.getProperty("aws_access_key_id", "");
        String awsSecretAccessKey = System.getProperty("aws_secret_access_key", "");
        String awsRegion = System.getProperty("aws_region", "us-east-1");

        this.s3Client = S3Client.builder()
                .region(Region.of(awsRegion)) // Use the region from system property
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey))) // Use credentials from system property
                .build();

        this.bucketName = bucketName;
        this.fileMetadataRepository = fileMetadataRepository;
    }

    @Override
    public Object uploadFileToS3(MultipartFile file) throws IOException {
        System.out.println("Starting file upload...");
        String filename = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
        System.out.println("Generated filename: " + filename);

        try (InputStream inputStream = file.getInputStream()) {
            System.out.println("Preparing to upload to S3...");

            // Create the PutObjectRequest without using a temp file
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build();

            System.out.println("Uploading file to S3 with request: " + putObjectRequest);

            try {
                s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
                System.out.println("File uploaded successfully to S3.");
            } catch (Exception e) {
                System.out.println("Error uploading file to S3: " + e.getMessage());
                e.printStackTrace(); 
                return "Error uploading file to S3: " + e.getMessage();
            }

            // Save metadata to local DB (e.g., MySQL)
            FileUploadMetaData fileMetadata = new FileUploadMetaData(filename, file.getContentType(), file.getSize(), "s3://" + bucketName + "/" + filename);
            fileMetadataRepository.save(fileMetadata);
            System.out.println("File metadata saved to database.");

            // Create response object directly here
            String fileName = fileMetadata.getFilename();
            String fileId = fileMetadata.getId();
            String filePath = fileMetadata.getFilePath();
            String uploadDate = Instant.now().toString(); // Use the current timestamp for upload date

            return new Object() {
                public String file_name = fileName;
                public String id = fileId;
                public String url = filePath;
                public String upload_date = uploadDate;
            };
        }
    }

    @Override
    public Object getFileUrlFromS3(String id) {
        // Retrieve the file metadata from the database using the file ID
        Optional<FileUploadMetaData> fileMetaDataOptional = fileMetadataRepository.findById(id);
        if (fileMetaDataOptional.isPresent()) {
            FileUploadMetaData fileMeta = fileMetaDataOptional.get();

            // Prepare the response object with the required fields
            String fileName = fileMeta.getFilename();
            String fileId = fileMeta.getId();
            String filePath = fileMeta.getFilePath(); // URL is stored in the file path
            String uploadDate = Instant.now().toString(); // You can store the actual upload date in the model if required

            // Create a response object (anonymous class with file details)
            var response = new Object() {
                public String file_name = fileName;
                public String id = fileId;
                public String url = filePath;
                public String upload_date = uploadDate;
            };

            return response;
        } else {
            throw new RuntimeException("File not found for ID: " + id); // Handle file not found scenario
        }
    }

    @Override
    public void deleteFileFromS3(String id) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(id)
                .build();
        s3Client.deleteObject(deleteObjectRequest);

        // Delete metadata from local MySQL database
        fileMetadataRepository.deleteById(id);
    }
}
