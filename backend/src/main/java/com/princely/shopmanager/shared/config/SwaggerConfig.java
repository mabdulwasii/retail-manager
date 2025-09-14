package com.princely.shopmanager.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for Swagger/OpenAPI 3 documentation.
 *
 * This configuration provides:
 * - API documentation with detailed descriptions
 * - Security schemes for OAuth2/JWT authentication
 * - Server configuration for different environments
 * - Contact and license information
 * - API versioning and tagging
 */
@Configuration
public class SwaggerConfig {

    @Value("${app.swagger.title:Shop Manager API}")
    private String title;

    @Value("${app.swagger.description:Comprehensive API for Shop Manager - A multi-tenant retail management platform}")
    private String description;

    @Value("${app.swagger.version:1.0.0}")
    private String version;

    @Value("${app.swagger.contact.name:Shop Manager Development Team}")
    private String contactName;

    @Value("${app.swagger.contact.email:dev@shopmanager.com}")
    private String contactEmail;

    @Value("${app.swagger.contact.url:https://shopmanager.com}")
    private String contactUrl;

    @Value("${server.port:8081}")
    private String serverPort;

    /**
     * Configures the main OpenAPI specification for the Shop Manager API.
     *
     * This bean defines:
     * - API metadata (title, description, version)
     * - Security schemes (JWT Bearer tokens)
     * - Server configurations for different environments
     * - Contact and license information
     * - API tags for organizing endpoints
     *
     * @return Configured OpenAPI instance
     */
    @Bean
    public OpenAPI shopManagerOpenAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .servers(serverList())
            .tags(tagList())
            .components(securityComponents())
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }

    /**
     * Creates API information including title, description, version, and contact details.
     *
     * @return Info object with API metadata
     */
    private Info apiInfo() {
        return new Info()
            .title(title)
            .description(description)
            .version(version)
            .contact(new Contact()
                .name(contactName)
                .email(contactEmail)
                .url(contactUrl))
            .license(new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * Defines server configurations for different deployment environments.
     *
     * @return List of Server objects representing different environments
     */
    private List<Server> serverList() {
        return List.of(
            new Server()
                .url("http://localhost:" + serverPort)
                .description("Development server"),
            new Server()
                .url("https://api-staging.shopmanager.com")
                .description("Staging server"),
            new Server()
                .url("https://api.shopmanager.com")
                .description("Production server")
        );
    }

    /**
     * Creates API tags for organizing endpoints by functional area.
     *
     * @return List of Tag objects for API organization
     */
    private List<Tag> tagList() {
        return List.of(
            new Tag()
                .name("Shop Management")
                .description("Operations for managing shop settings, configuration, and customization"),
            new Tag()
                .name("Product Management")
                .description("Product catalog, inventory, and pricing operations"),
            new Tag()
                .name("Sales Management")
                .description("Sales transactions, receipts, and point-of-sale operations"),
            new Tag()
                .name("Investment Management")
                .description("Investment tracking, profit distribution, and investor relations"),
            new Tag()
                .name("Analytics & Reporting")
                .description("Business analytics, dashboards, and reporting functionality"),
            new Tag()
                .name("User Management")
                .description("User accounts, roles, permissions, and authentication"),
            new Tag()
                .name("Feature Flags")
                .description("Feature toggle management and configuration"),
            new Tag()
                .name("Audit & Security")
                .description("Audit logs, security events, and compliance reporting"),
            new Tag()
                .name("Fraud Detection")
                .description("Fraud detection rules, risk assessment, and security monitoring"),
            new Tag()
                .name("System Administration")
                .description("System-level operations, configuration, and maintenance")
        );
    }

    /**
     * Configures security schemes for API authentication.
     *
     * Defines multiple authentication methods:
     * - JWT Bearer tokens (primary method)
     * - Basic authentication (fallback for admin access)
     *
     * @return Components object with security scheme definitions
     */
    private Components securityComponents() {
        return new Components()
            .addSecuritySchemes("bearerAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("JWT Bearer token obtained from Keycloak authentication"))
            .addSecuritySchemes("basicAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("basic")
                .description("Basic authentication for administrative access to Swagger UI"));
    }
}