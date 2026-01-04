package com.princely.shopmanager.embedded.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * Cloud sync configuration for embedded mode.
 * Enables syncing local transaction data to cloud for analytics.
 */
@Data
@Configuration
@Profile("embedded")
@ConfigurationProperties(prefix = "application.sync")
@ConditionalOnProperty(name = "application.sync.enabled", havingValue = "true")
public class CloudSyncConfig {

    /**
     * Cloud endpoint URL
     */
    private String cloudEndpoint;

    /**
     * API key for authentication
     */
    private String apiKey;

    /**
     * Store identifier
     */
    private String storeId;

    /**
     * Schedule configuration
     */
    private Schedule schedule = new Schedule();

    /**
     * Privacy configuration
     */
    private Privacy privacy = new Privacy();

    /**
     * Resilience configuration
     */
    private Resilience resilience = new Resilience();

    @Data
    public static class Schedule {
        private String cron = "0 0 * * * ?"; // Every hour
        private int batchSize = 100;
        private int retryMaxAttempts = 3;
        private long retryBackoffMs = 60000; // 1 minute
    }

    @Data
    public static class Privacy {
        private boolean anonymizePii = false;
        private List<String> fieldsToAnonymize = List.of(
                "customerName",
                "customerPhone",
                "customerEmail"
        );
    }

    @Data
    public static class Resilience {
        private boolean circuitBreakerEnabled = true;
        private int failureThreshold = 5;
        private long timeoutSeconds = 30;
    }
}
