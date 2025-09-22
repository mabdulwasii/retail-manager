package com.princely.shopmanager.auth.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for Keycloak Admin Client
 */
@Configuration
@Slf4j
public class KeycloakAdminConfig {

    @Value("${keycloak.auth-server-url:http://shop-manager-keycloak:8080}")
    private String keycloakUrl;

    @Value("${keycloak.realm:shop-manager}")
    private String realm;

    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin.password:KeycloakAdm1n@2024!SecureAuth#CompliantPassword}")
    private String adminPassword;

    @Value("${keycloak.admin.client-id:admin-cli}")
    private String adminClientId;

    @Bean
    public Keycloak keycloakAdmin() {
        log.info("Initializing Keycloak Admin Client for URL: {}", keycloakUrl);

        try {
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakUrl)
                    .realm("master") // Admin client uses master realm
                    .clientId(adminClientId)
                    .username(adminUsername)
                    .password(adminPassword)
                    .build();

            // Test the connection
            keycloak.tokenManager().getAccessToken();
            log.info("Keycloak Admin Client initialized successfully");

            return keycloak;
        } catch (Exception e) {
            log.error("Failed to initialize Keycloak Admin Client", e);
            throw new RuntimeException("Failed to connect to Keycloak", e);
        }
    }
}