package com.cloud.webapp.model;

import lombok.*;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "file_upload_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadMetaData {

    @Id
    private String id;

    private String filename;
    private String contentType;
    private long fileSize;
    private String filePath;

    public FileUploadMetaData(String filename, String contentType, long fileSize, String filePath) {
        this.id = UUID.randomUUID().toString();  // Manually generate unique ID using UUID
        this.filename = filename;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.filePath = filePath;
    }
}
