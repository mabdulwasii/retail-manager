package com.princely.shopmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.modulith.Modulith;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = FlywayAutoConfiguration.class)
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
@EnableKafka
@ConfigurationPropertiesScan
@Modulith(systemName = "Shop Manager", sharedModules = {"shared"})
public class ShopManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopManagerApplication.class, args);
    }
}