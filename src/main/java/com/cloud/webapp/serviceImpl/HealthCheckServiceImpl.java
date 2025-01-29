package com.cloud.webapp.serviceImpl;

import com.cloud.webapp.model.HealthCheck;
import com.cloud.webapp.repository.HealthCheckRepository;
import com.cloud.webapp.service.HealthCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
public class HealthCheckServiceImpl implements HealthCheckService {

    @Autowired
    private HealthCheckRepository healthCheckRepository;

    @Override
    public void performHealthCheck() {
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setDateTime(Instant.now());
        healthCheckRepository.save(healthCheck);
    }
}
