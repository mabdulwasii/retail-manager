package com.princely.shopmanager.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties for application security settings.
 *
 * This class centralizes security-related configuration including CORS,
 * JWT settings, and other security policies that can be configured
 * through application properties or environment variables.
 *
 * @author Shop Manager Development Team
 * @version 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityConfig {

    /**
     * CORS configuration settings
     */
    private final Cors cors = new Cors();

    /**
     * JWT configuration settings
     */
    private final Jwt jwt = new Jwt();

    /**
     * Session configuration settings
     */
    private final Session session = new Session();

    @Data
    public static class Cors {
        /**
         * List of allowed origins for CORS requests.
         * Configurable via APP_SECURITY_CORS_ALLOWED_ORIGINS environment variable (comma-separated).
         * Default includes localhost URLs for development.
         */
        private List<String> allowedOrigins = new java.util.ArrayList<>(List.of(
            "http://localhost:3000",
            "http://localhost:3001",
            "http://localhost:3002"
        ));

        /**
         * List of allowed HTTP methods for CORS requests.
         * Configurable via APP_SECURITY_CORS_ALLOWED_METHODS environment variable (comma-separated).
         */
        private List<String> allowedMethods = new java.util.ArrayList<>(List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        /**
         * List of allowed headers for CORS requests.
         * Configurable via APP_SECURITY_CORS_ALLOWED_HEADERS environment variable (comma-separated).
         */
        private List<String> allowedHeaders = new java.util.ArrayList<>(List.of("*"));

        /**
         * Whether to allow credentials in CORS requests.
         */
        private boolean allowCredentials = true;

        /**
         * Maximum age for CORS preflight cache in seconds.
         */
        private Long maxAge = 3600L;
    }

    @Data
    public static class Jwt {
        /**
         * JWT token expiration time in hours.
         */
        private int expirationHours = 24;

        /**
         * Whether to validate token issuer.
         */
        private boolean validateIssuer = true;

        /**
         * Whether to validate token audience.
         */
        private boolean validateAudience = true;

        /**
         * Clock skew tolerance in seconds.
         */
        private int clockSkewSeconds = 60;
    }

    @Data
    public static class Session {
        /**
         * Session timeout in minutes.
         */
        private int timeoutMinutes = 30;

        /**
         * Whether to use secure cookies.
         */
        private boolean secureCookies = true;

        /**
         * Whether to use HTTP-only cookies.
         */
        private boolean httpOnlyCookies = true;

        /**
         * Cookie same-site policy.
         */
        private String sameSitePolicy = "Strict";
    }
}