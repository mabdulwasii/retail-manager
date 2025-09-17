package com.princely.shopmanager.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 configuration for Shop Manager API documentation.
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.swagger.title:Shop Manager API}")
    private String title;

    @Value("${app.swagger.description:Comprehensive API for Shop Manager}")
    private String description;

    @Value("${app.swagger.version:1.0.0}")
    private String version;

    @Value("${app.swagger.contact.name:Shop Manager Development Team}")
    private String contactName;

    @Value("${app.swagger.contact.email:dev@shopmanager.com}")
    private String contactEmail;

    @Value("${app.swagger.contact.url:https://shopmanager.com}")
    private String contactUrl;

    @Bean
    public OpenAPI shopManagerOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title(title)
                .description(description)
                .version(version)
                .contact(new Contact()
                    .name(contactName)
                    .email(contactEmail)
                    .url(contactUrl))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT Bearer token obtained from Keycloak authentication")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}