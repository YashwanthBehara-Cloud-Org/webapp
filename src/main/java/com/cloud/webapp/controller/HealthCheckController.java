package com.cloud.webapp.controller;

import com.cloud.webapp.exception.DataBaseConnectionException;
import com.cloud.webapp.service.HealthCheckService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @Autowired
    private HealthCheckService healthCheckService;

    @Autowired
    private MeterRegistry meterRegistry;

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);

    @GetMapping("/healthz")
    public ResponseEntity<Void> performHealthCheck(HttpServletRequest request) {

        Timer.Sample sample = Timer.start(meterRegistry);
        meterRegistry.counter("api.healthz.getHealthz.count").increment();

        validateRequest(request);

        logger.info("Get - Health check endpoint hit");

        try {
            healthCheckService.performHealthCheck();
        } catch (Exception e) {
            logger.error("Get - Health check failed: {}", e.getMessage());
            throw new DataBaseConnectionException("Database connection failed", e);
        }
        finally {
            sample.stop(meterRegistry.timer("api.get.healthz.timer"));
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .build();
    }

    @GetMapping("/cicd")
    public ResponseEntity<Void> performCicdHealthCheck(HttpServletRequest request) {

        Timer.Sample sample = Timer.start(meterRegistry);
        meterRegistry.counter("api.healthz.getHealthz.count").increment(); // Reusing metric

        validateRequest(request);

        logger.info("Get - CICD health check endpoint hit");

        try {
            healthCheckService.performHealthCheck();
        } catch (Exception e) {
            logger.error("Get - CICD health check failed: {}", e.getMessage());
            throw new DataBaseConnectionException("Database connection failed", e);
        } finally {
            sample.stop(meterRegistry.timer("api.get.healthz.timer")); // Reusing timer
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

