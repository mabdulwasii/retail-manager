package com.princely.shopmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.modulith.Modulith;

@Modulith
@SpringBootApplication(scanBasePackages = "com.princely.shopmanager")
@ConfigurationPropertiesScan(basePackages = "com.princely.shopmanager")
@EnableJpaAuditing
@EnableKafka
public class ShopManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopManagerApplication.class, args);
    }
}