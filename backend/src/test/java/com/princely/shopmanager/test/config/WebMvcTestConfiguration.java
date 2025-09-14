package com.princely.shopmanager.test.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Minimal test configuration for controller tests.
 * Excludes JPA and data-related configurations that are not needed for web layer testing.
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
    org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
    org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
    org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class
})
@EnableMethodSecurity(prePostEnabled = true)
public class WebMvcTestConfiguration {
}