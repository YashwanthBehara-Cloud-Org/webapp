
package com.cloud.webapp.serviceImpl;

import com.cloud.webapp.exception.DataBaseConnectionException;
import com.cloud.webapp.model.FileUploadMetaData;
import com.cloud.webapp.repository.FileUploadMetaDataRepository;
import com.cloud.webapp.service.FileUploadMetaDataService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
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
    private final MeterRegistry meterRegistry;


    private static final Logger logger = LoggerFactory.getLogger(FileUploadMetaDataServiceImpl.class);

    public FileUploadMetaDataServiceImpl(@Value("${AWS_S3_BUCKET_NAME}") String bucketName,
                                         FileUploadMetaDataRepository fileMetadataRepository,
                                         MeterRegistry meterRegistry) {

        // Fetching credentials from system properties or environment variables
        String awsAccessKeyId = System.getProperty("aws.accessKeyId", "");
        String awsSecretAccessKey = System.getProperty("aws.secretAccessKey", "");
        String awsRegion = System.getProperty("aws.region", "us-east-1");

        // If the credentials are provided, use them. Otherwise, fall back to default credentials provider (IAM role for EC2 or AWS credentials file)
        if (!awsAccessKeyId.isEmpty() && !awsSecretAccessKey.isEmpty()) {
            this.s3Client = S3Client.builder()
                    .region(Region.of(awsRegion))  // Use the region from the system property
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey)))  // Use the provided credentials
                    .build();
        } else {
            this.s3Client = S3Client.builder()
                    .region(Region.of(awsRegion))  // Use the region from the system property
                    .credentialsProvider(DefaultCredentialsProvider.create())  // Use the default credentials provider (IAM role or AWS credentials file)
                    .build();
        }

        this.bucketName = bucketName;
        this.fileMetadataRepository = fileMetadataRepository;
        this.meterRegistry = meterRegistry;

    }

    @Override
    public Object uploadFileToS3(MultipartFile file) throws IOException {
        System.out.println("Starting file upload...");
        logger.info("Starting upload for file: {}", file.getOriginalFilename());

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

                Timer.Sample s3Sample = Timer.start(meterRegistry);
                s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
                s3Sample.stop(meterRegistry.timer("aws.s3.file.upload.timer"));

                logger.info("S3 upload complete");

                System.out.println("File uploaded successfully to S3.");
            } catch (Exception e) {
                System.out.println("Error uploading file to S3: " + e.getMessage());
                logger.error("S3 upload error: ", e);

                e.printStackTrace(); 
                return "Error uploading file to S3: " + e.getMessage();
            }

            // Save metadata to local DB (e.g., MySQL)
            FileUploadMetaData fileMetadata = new FileUploadMetaData(filename, file.getContentType(), file.getSize(), "s3://" + bucketName + "/" + filename);

            try {
                Timer.Sample dbSample = Timer.start(meterRegistry);
                fileMetadataRepository.save(fileMetadata);
                dbSample.stop(meterRegistry.timer("db.file.insert.timer"));
            } catch (Exception e) {
                throw new DataBaseConnectionException("Failed to connect to DB", e);
            }

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

        Optional<FileUploadMetaData> fileMetaDataOptional;
        try {
            Timer.Sample dbSample = Timer.start(meterRegistry);
            fileMetaDataOptional = fileMetadataRepository.findById(id);
            dbSample.stop(meterRegistry.timer("db.file.fetch.timer"));
            logger.info("Fetched metadata from DB for file ID: {}", id);
        } catch (Exception e) {
            logger.error("DB connection failed while fetching file ID: {}", id, e);
            throw new DataBaseConnectionException("Failed to connect to DB", e);
        }

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
        // Check if the file exists in the database

        Optional<FileUploadMetaData> fileMetaDataOptional;
        try {
            Timer.Sample dbSample = Timer.start(meterRegistry);
            fileMetaDataOptional = fileMetadataRepository.findById(id);
            dbSample.stop(meterRegistry.timer("db.file.find.timer"));
            logger.info("Metadata lookup complete for delete. File found: {}", fileMetaDataOptional.isPresent());
        } catch (Exception e) {
            logger.error("DB connection failed while fetching file ID for delete: {}", id, e);
            throw new DataBaseConnectionException("Failed to connect to DB", e);
        }

        if (fileMetaDataOptional.isEmpty()) {
            throw new RuntimeException("File with ID " + id + " not found. Cannot delete.");
        }

        // Retrieve file metadata
        FileUploadMetaData fileMetadata = fileMetaDataOptional.get();
        String fileKey = fileMetadata.getFilename();

        // Delete the file from S3
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        Timer.Sample s3Sample = Timer.start(meterRegistry);
        s3Client.deleteObject(deleteObjectRequest);
        s3Sample.stop(meterRegistry.timer("aws.s3.file.delete.timer"));

        try {
            Timer.Sample dbDeleteSample = Timer.start(meterRegistry);
            fileMetadataRepository.deleteById(id);
            dbDeleteSample.stop(meterRegistry.timer("db.file.delete.timer"));
            logger.info("Metadata deleted from DB for file ID: {}", id);
        } catch (Exception e) {
            logger.error("DB deletion failed for file ID: {}", id, e);
            throw new DataBaseConnectionException("Failed to connect to DB", e);
        }
        
   }

}
