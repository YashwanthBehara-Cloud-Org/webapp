package com.cloud.webapp.controller;

import com.cloud.webapp.exception.DataBaseConnectionException;
import com.cloud.webapp.service.HealthCheckService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import io.github.cdimascio.dotenv.Dotenv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(HealthCheckController.class)
@Import(TestConfig.class)

public class HealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HealthCheckService healthCheckService; // Now injected from TestConfig

    @BeforeAll
    static void setupEnvironment() {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.setProperty("DB_URL", dotenv.get("DB_URL"));
    }

    @BeforeEach
    void setUp() {
        doNothing().when(healthCheckService).performHealthCheck();
    }

    @Test
    void testMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/healthz"))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(put("/healthz"))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(delete("/healthz"))
                .andExpect(status().isMethodNotAllowed());
    }


    @Test
    void testDatabaseFailure() throws Exception {
        doThrow(new DataBaseConnectionException("Database connection failed", new RuntimeException()))
                .when(healthCheckService).performHealthCheck();

        mockMvc.perform(get("/healthz"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DataBaseConnectionException))
                .andExpect(result -> assertEquals("Database connection failed", result.getResolvedException().getMessage()));
    }

    @Test
    void testGetRequestWithPayload() throws Exception {
        String jsonData = "{\"id\":7}";

        mockMvc.perform(get("/healthz")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonData.getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalArgumentException))
                .andExpect(result -> assertEquals("Payload Not Allowed", result.getResolvedException().getMessage()));
    }

    @Test
    void testHealthCheckSuccess() throws Exception {
        mockMvc.perform(get("/healthz"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate"))
                .andExpect(header().string(HttpHeaders.PRAGMA, "no-cache"));
    }


}
