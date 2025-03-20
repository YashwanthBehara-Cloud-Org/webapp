package com.cloud.webapp.controller;

import com.cloud.webapp.model.HealthCheck;
import com.cloud.webapp.repository.HealthCheckRepository;
import com.cloud.webapp.serviceImpl.HealthCheckServiceImpl;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
        // Check if running in a CI environment by checking the CI environment variable
        String isCi = System.getenv("CI");

        if (isCi == null || !isCi.equals("true")) {
            // In Local/Production environment, load .env file using Dotenv
            Dotenv dotenv = Dotenv.load();
            System.setProperty("DB_URL", dotenv.get("DB_URL"));
            System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
            System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
            System.setProperty("AWS_S3_BUCKET_NAME", dotenv.get("AWS_S3_BUCKET_NAME"));
            System.setProperty("aws_access_key_id", dotenv.get("aws_access_key_id", ""));
            System.setProperty("aws_secret_access_key", dotenv.get("aws_secret_access_key", ""));
            System.setProperty("aws_region", dotenv.get("aws_region", "us-east-1"));
        } else {
            // In CI environment (e.g., GitHub Actions), read from system environment variables
            System.setProperty("DB_URL", System.getenv("DB_URL") != null ? System.getenv("DB_URL") : "");
            System.setProperty("DB_USERNAME", System.getenv("DB_USERNAME") != null ? System.getenv("DB_USERNAME") : "");
            System.setProperty("DB_PASSWORD", System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "");
            System.setProperty("AWS_S3_BUCKET_NAME", System.getenv("AWS_S3_BUCKET_NAME") != null ? System.getenv("AWS_S3_BUCKET_NAME") : "");
            System.setProperty("aws_access_key_id", System.getenv("aws_access_key_id") != null ? System.getenv("aws_access_key_id") : "");
            System.setProperty("aws_secret_access_key", System.getenv("aws_secret_access_key") != null ? System.getenv("aws_secret_access_key") : "");
            System.setProperty("aws_region", System.getenv("aws_region") != null ? System.getenv("aws_region") : "us-east-1");

        }
    }

    @Test
    @Transactional
    public void testDatabaseConnectionAndInsert() {
        try {
            // Perform the health check, which should insert a dummy record into the DB
            healthCheckServiceImpl.performHealthCheck();

            // Check if the dummy record was inserted by querying the most recent one
            Optional<HealthCheck> healthCheck = healthCheckRepository.findTopByOrderByCheckIdDesc();

            // Assert that the record exists and the dateTime is not null
            assertTrue(healthCheck.isPresent(), "HealthCheck record should be inserted into the database");
            assertNotNull(healthCheck.get().getDateTime(), "HealthCheck datetime should not be null");

        } catch (Exception e) {
            // Catch the exception if the health check fails and assert failure
            fail("HealthCheck insertion failed with exception: " + e.getMessage());
        }
    }
}
