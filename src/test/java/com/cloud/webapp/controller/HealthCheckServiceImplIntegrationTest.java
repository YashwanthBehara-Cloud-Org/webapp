package com.cloud.webapp.controller;

import com.cloud.webapp.model.HealthCheck;
import com.cloud.webapp.repository.HealthCheckRepository;
import com.cloud.webapp.serviceImpl.HealthCheckServiceImpl;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class HealthCheckServiceImplIntegrationTest {


    @Autowired
    private HealthCheckServiceImpl healthCheckServiceImpl;

    @Autowired
    private HealthCheckRepository healthCheckRepository;

    @BeforeAll
    public static void setUp() {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.setProperty("DB_URL", dotenv.get("DB_URL"));
    }

    @Test
    public void testDatabaseConnectionAndInsert() {
        try {
            healthCheckServiceImpl.performHealthCheck();
            Optional<HealthCheck> healthCheck = healthCheckRepository.findTopByOrderByCheckIdDesc();

            assertTrue(healthCheck.isPresent(), "HealthCheck record should be inserted into the database");
            assertNotNull(healthCheck.get().getDateTime(), "HealthCheck datetime should not be null");

        } catch (Exception e) {
            fail("HealthCheck insertion failed with exception: " + e.getMessage());
        }
    }
    }
