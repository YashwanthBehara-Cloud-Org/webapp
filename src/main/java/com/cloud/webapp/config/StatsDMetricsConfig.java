package com.cloud.webapp.config;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.statsd.StatsdConfig;
import io.micrometer.statsd.StatsdFlavor;
import io.micrometer.statsd.StatsdMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class StatsDMetricsConfig {

    @Bean
    public StatsdConfig statsdConfig() {
        return new StatsdConfig() {
            @Override
            public String get(String key) {
                return null; // default values
            }

            @Override
            public StatsdFlavor flavor() {
                return StatsdFlavor.ETSY;
            }

            @Override
            public String host() {
                return "localhost"; // On EC2, use 127.0.0.1 or the agent IP
            }

            @Override
            public int port() {
                return 8125;
            }

            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public Duration pollingFrequency() {
                return Duration.ofSeconds(10);
            }

            @Override
            public String prefix() {
                return "webapp"; // shows in CloudWatch
            }
        };
    }

    @Bean
    public MeterRegistry meterRegistry(StatsdConfig config) {
        return new StatsdMeterRegistry(config, Clock.SYSTEM);
    }
}
