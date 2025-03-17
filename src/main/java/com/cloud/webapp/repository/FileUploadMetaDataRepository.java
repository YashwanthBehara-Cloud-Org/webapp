package com.cloud.webapp.repository;

import com.cloud.webapp.model.FileUploadMetaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileUploadMetaDataRepository extends JpaRepository<FileUploadMetaData, String> {
    void deleteById(String id);
}
