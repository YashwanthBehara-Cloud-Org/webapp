package com.cloud.webapp.controller;

import com.cloud.webapp.exception.DataBaseConnectionException;
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
        validateRequest(request);

        try {
            healthCheckService.performHealthCheck();
        } catch (Exception e) {
            throw new DataBaseConnectionException("Database connection failed", e);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .build();
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