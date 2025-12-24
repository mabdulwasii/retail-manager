package com.princely.shopmanager.test.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.Map;

/**
 * Test JWT Security Configuration for real authentication testing.
 * <p>
 * This configuration is activated only for the 'test-security' profile.
 * It provides a JwtDecoder that accepts test JWT tokens without signature validation.
 * <p>
 * Use this for integration tests that need to test real Spring Security authorization
 * while still using mock JWT tokens.
 */
@TestConfiguration
@Profile("test-security")
public class TestJwtSecurityConfig {

    /**
     * Test JwtDecoder that accepts JWT tokens without signature validation.
     * <p>
     * Parses the JWT token payload and creates a Jwt object with the claims.
     * This allows testing real Spring Security authorization logic without needing
     * a running Keycloak server or real JWT signing.
     *
     * @return JwtDecoder for test JWT tokens
     */
    @Bean
    @Primary
    public JwtDecoder testJwtDecoder(ObjectMapper objectMapper) {
        return token -> {
            try {
                // Parse the test token format: "test-token.{base64-payload}.signature"
                String[] parts = token.split("\\.");
                if (parts.length < 2) {
                    throw new JwtException("Invalid JWT token format");
                }

                // Decode the payload (second part)
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));

                // Parse JSON using ObjectMapper
                Map<String, Object> claims = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});

                // Extract standard claims
                Object iatObj = claims.getOrDefault("iat", System.currentTimeMillis() / 1000);
                long iat = iatObj instanceof Number ? ((Number) iatObj).longValue() : Long.parseLong(iatObj.toString());
                Instant issuedAt = Instant.ofEpochSecond(iat);

                Object expObj = claims.getOrDefault("exp", iat + 3600);
                long exp = expObj instanceof Number ? ((Number) expObj).longValue() : Long.parseLong(expObj.toString());
                Instant expiresAt = Instant.ofEpochSecond(exp);

                // Create Jwt object
                return new Jwt(
                    token,
                    issuedAt,
                    expiresAt,
                    Map.of("alg", "none", "typ", "JWT"), // headers
                    claims
                );

            } catch (Exception e) {
                throw new JwtException("Failed to decode test JWT token", e);
            }
        };
    }
}
