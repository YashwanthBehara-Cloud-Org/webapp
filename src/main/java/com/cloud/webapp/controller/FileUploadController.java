package com.cloud.webapp.controller;

import com.cloud.webapp.exception.FileNotFoundException;
import com.cloud.webapp.exception.InvalidFileException;
import com.cloud.webapp.model.FileUploadMetaData;
import com.cloud.webapp.service.FileUploadMetaDataService;
import jakarta.servlet.http.HttpServletRequest;
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

    @PostMapping
    public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile[] files, HttpServletRequest request) {
        try {

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
            // Upload the file and get the URL path
            Object fileResponse = fileService.uploadFileToS3(files[0]);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .body(fileResponse);  // Return the response object with the desired fields
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .body("Invalid request format: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getFile(@PathVariable String id, HttpServletRequest request) {
        validateRequest(request); // Enforce validation

        try {
            Object fileResponse = fileService.getFileUrlFromS3(id);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .body(fileResponse);  // Return the file metadata as response
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .body("File not found: " + e.getMessage());
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

        validateRequest(request); // Enforce validation

        try {
            fileService.deleteFileFromS3(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .build(); // Return no content on successful deletion
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .build();
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
