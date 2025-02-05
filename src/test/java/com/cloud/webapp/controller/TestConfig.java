package com.cloud.webapp.controller;

import com.cloud.webapp.service.HealthCheckService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestConfig {

    @Bean
    @Primary
    public HealthCheckService healthCheckService() {
        return Mockito.mock(HealthCheckService.class);
    }
}