package com.cloud.webapp.serviceImpl;

import com.cloud.webapp.model.HealthCheck;
import com.cloud.webapp.repository.HealthCheckRepository;
import com.cloud.webapp.service.HealthCheckService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
public class HealthCheckServiceImpl implements HealthCheckService {


    private final HealthCheckRepository healthCheckRepository;
    private final MeterRegistry meterRegistry;
    private static final Logger logger = LoggerFactory.getLogger(FileUploadMetaDataServiceImpl.class);


    public HealthCheckServiceImpl(HealthCheckRepository healthCheckRepository, MeterRegistry meterRegistry) {
        this.healthCheckRepository = healthCheckRepository;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void performHealthCheck() {
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setDateTime(Instant.now());

        Timer.Sample dbSample = Timer.start(meterRegistry);
        healthCheckRepository.save(healthCheck);
        dbSample.stop(meterRegistry.timer("db.healthz.insert.timer"));
        logger.info("Record inserted in Health DB");

    }
}
