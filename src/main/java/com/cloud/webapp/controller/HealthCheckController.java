package com.cloud.webapp.controller;

import com.cloud.webapp.exception.DatabaseConnectionException;
import com.cloud.webapp.service.HealthCheckService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/healthz")
public class HealthCheckController {

    @Autowired
    private HealthCheckService healthCheckService;

    @GetMapping
    public ResponseEntity<Void> performHealthCheck(HttpServletRequest request) {

        // Check if any query parameters are present
        if (!request.getParameterMap().isEmpty()) {
            throw new IllegalArgumentException("Query parameters are not allowed");
        }

        // Check if Request body is present
        if (request.getContentLengthLong() > 0) {
            throw new IllegalArgumentException("Payload Not Allowed");
        }

        try{
            healthCheckService.performHealthCheck();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .build();
        }

        //  If Database connection is not successful
        catch (Exception e){
            throw new DatabaseConnectionException("Database connection failed", e);
        }

    }
}