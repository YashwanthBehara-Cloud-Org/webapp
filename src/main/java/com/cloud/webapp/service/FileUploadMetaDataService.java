package com.cloud.webapp.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadMetaDataService {

    Object uploadFileToS3(MultipartFile file) throws Exception;

    Object getFileUrlFromS3(String id) throws Exception;

    void deleteFileFromS3(String id) throws Exception;
}
