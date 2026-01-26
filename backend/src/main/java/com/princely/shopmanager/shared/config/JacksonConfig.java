package com.princely.shopmanager.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson ObjectMapper configuration for JSON serialization/deserialization.
 * Uses Spring's Jackson2ObjectMapperBuilder to respect application.yml settings.
 * Handles Hibernate proxies and lazy-loaded entities.
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        // Use Spring's builder - respects application.yml settings including timezone
        ObjectMapper mapper = builder.build();

        // Register Hibernate6 module to handle lazy-loaded entities and proxies
        Hibernate6Module hibernateModule = new Hibernate6Module();
        // Don't force lazy loading - serialize IDs only for lazy-loaded entities
        hibernateModule.disable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
        // Replace lazy-loaded entities with null/empty instead of failing
        hibernateModule.enable(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS);
        mapper.registerModule(hibernateModule);

        return mapper;
    }
}
