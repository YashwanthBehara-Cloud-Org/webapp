package com.cloud.webapp.controller;

import com.cloud.webapp.config.StatsDMetricsConfig;
import com.cloud.webapp.exception.DataBaseConnectionException;
import com.cloud.webapp.service.HealthCheckService;
import com.cloud.webapp.util.TestEnvironmentLoader;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import io.github.cdimascio.dotenv.Dotenv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = HealthCheckController.class, excludeAutoConfiguration = StatsDMetricsConfig.class)
@Import(TestConfig.class)

public class HealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HealthCheckService healthCheckService; // Injected from TestConfig

    @Autowired
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        doNothing().when(healthCheckService).performHealthCheck();
    }

    // Load environment variables before tests
    @BeforeAll
    static void setupEnvironment() {
        TestEnvironmentLoader.loadEnvironmentVariables();  // Load .env variables here
    }


    //  Invalid methods ( PUT, POST, DELETE, PATCH )
    @Test
    void testMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/healthz"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate"))
                .andExpect(header().string(HttpHeaders.PRAGMA, "no-cache"));

        mockMvc.perform(put("/healthz"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate"))
                .andExpect(header().string(HttpHeaders.PRAGMA, "no-cache"));
        ;

        mockMvc.perform(delete("/healthz"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate"))
                .andExpect(header().string(HttpHeaders.PRAGMA, "no-cache"));;

        mockMvc.perform(patch("/healthz"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate"))
                .andExpect(header().string(HttpHeaders.PRAGMA, "no-cache"));;
    }

    //  Database connection error
    @Test
    void testDatabaseFailure() throws Exception {
        doThrow(new DataBaseConnectionException("Database connection failed", new RuntimeException()))
                .when(healthCheckService).performHealthCheck();

        mockMvc.perform(get("/healthz"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DataBaseConnectionException))
                .andExpect(result -> assertEquals("Database connection failed", result.getResolvedException().getMessage()))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate"))
                .andExpect(header().string(HttpHeaders.PRAGMA, "no-cache"));
    }

    //  GET request with a request payload
    @Test
    void testGetRequestWithPayload() throws Exception {
        String jsonData = "{\"id\":7}";

        mockMvc.perform(get("/healthz")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonData))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalArgumentException))
                .andExpect(result -> assertEquals("Payload Not Allowed", result.getResolvedException().getMessage()))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate"))
                .andExpect(header().string(HttpHeaders.PRAGMA, "no-cache"));;
    }

    //  GET request with a query parameter
    @Test
    void testGetRequestWithQueryParameters() throws Exception {

        mockMvc.perform(get("/healthz")
                        .param("id", "123"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalArgumentException))
                .andExpect(result -> assertEquals("Query parameters are not allowed", result.getResolvedException().getMessage()))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate"))
                .andExpect(header().string(HttpHeaders.PRAGMA, "no-cache"));
    }

    // GET - success request
    @Test
    void testHealthCheckSuccess() throws Exception {
        mockMvc.perform(get("/healthz"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate"))
                .andExpect(header().string(HttpHeaders.PRAGMA, "no-cache"));
    }


}
