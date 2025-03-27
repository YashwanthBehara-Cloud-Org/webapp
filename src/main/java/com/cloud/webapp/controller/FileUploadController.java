package com.cloud.webapp.controller;

import com.cloud.webapp.exception.DataBaseConnectionException;
import com.cloud.webapp.exception.FileNotFoundException;
import com.cloud.webapp.exception.InvalidFileException;
import com.cloud.webapp.model.FileUploadMetaData;
import com.cloud.webapp.service.FileUploadMetaDataService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@RestController
@RequestMapping("/v1/file")
public class FileUploadController {


    @Autowired
    private FileUploadMetaDataService fileService;

    @Autowired
    private MeterRegistry meterRegistry;

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);


    @PostMapping
    public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile[] files, HttpServletRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        meterRegistry.counter("api.post.file.upload.count").increment();

        try {

            logger.info("POST - File Upload - Received request");

            // Validate: No query parameters allowed
            if (!request.getParameterMap().isEmpty()) {
                throw new IllegalArgumentException("Query parameters are not allowed.");
            }

            // Validate: Ensure a file is provided
            if (files == null || files.length == 0 || files[0].isEmpty()) {
                throw new IllegalArgumentException("A file must be provided.");
            }

            // Check if more than one file is uploaded
            if (files.length > 1) {
                throw new IllegalArgumentException("Only one file can be uploaded at a time.");
            }

            logger.info("Validations passed, calling fileService.uploadFileToS3()");

            // Upload the file and get the URL path
            Object fileResponse = fileService.uploadFileToS3(files[0]);

            logger.info("File uploaded successfully");

            return ResponseEntity.status(HttpStatus.CREATED)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .body(fileResponse);  // Return the response object with the desired fields
        
        } catch (DataBaseConnectionException ex) {
            // Let GlobalExceptionHandler handle it — rethrow
            throw ex;
        } catch (Exception e) {

            logger.error("File upload failed", e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .body("Invalid request format: " + e.getMessage());
        }
        finally {
            sample.stop(meterRegistry.timer("POST.file.upload.timer"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getFile(@PathVariable String id, HttpServletRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        meterRegistry.counter("api.get.file.count").increment();

        validateRequest(request); // Enforce validation
        logger.info("Fetching file with ID: {}", id);

        try {
            Object fileResponse = fileService.getFileUrlFromS3(id);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .body(fileResponse);  // Return the file metadata as response
        } catch (DataBaseConnectionException ex) {
            // Let GlobalExceptionHandler handle it — rethrow
            throw ex;
        } catch (Exception e) {

            logger.error("Error fetching file with ID {}: {}", id, e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .body("File not found: " + e.getMessage());
        }
        finally {
            sample.stop(meterRegistry.timer("GET.file.timer"));
        }
    }

    @GetMapping
    public ResponseEntity<Object> getFileBadRequest() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id, HttpServletRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        meterRegistry.counter("api.delete.file.count").increment();

        validateRequest(request); // Enforce validation

        logger.info("Request to delete file with ID: {}", id);


        try {
            fileService.deleteFileFromS3(id);

            logger.info("File with ID {} deleted successfully", id);

            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .build(); // Return no content on successful deletion
                    
        } catch (DataBaseConnectionException ex) {
            // Let GlobalExceptionHandler handle it — rethrow
            throw ex;
        } catch (Exception e) {

            logger.error("File deletion failed for ID {}: {}", id, e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .build();
        }
        finally {
            sample.stop(meterRegistry.timer("DELETE.file.timer"));
        }
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteFileBadRequest() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .build();
    }

    // Unsupported HTTP Methods (returns 405 Method Not Allowed)
    @RequestMapping(method = {RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.OPTIONS, RequestMethod.HEAD})
    public ResponseEntity<Object> handleMethodNotAllowed() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body("Method not allowed.");
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.OPTIONS, RequestMethod.HEAD, RequestMethod.POST})
    public ResponseEntity<Object> handleMethodNotAllowedWithId() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body("Method not allowed.");
    }

    private void validateRequest(HttpServletRequest request) {
        if (!request.getParameterMap().isEmpty()) {
            throw new IllegalArgumentException("Query parameters are not allowed");
        }

        if (request.getContentLengthLong() > 0) {
            throw new IllegalArgumentException("Payload Not Allowed");
        }
    }
}
