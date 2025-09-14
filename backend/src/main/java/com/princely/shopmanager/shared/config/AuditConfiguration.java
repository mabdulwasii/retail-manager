package com.princely.shopmanager.shared.config;

import com.princely.shopmanager.shared.listener.EntityAuditListener;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.audit.enabled", havingValue = "true", matchIfMissing = true)
public class AuditConfiguration {

    @Bean
    public EntityAuditListener entityAuditListener() {
        return new EntityAuditListener();
    }
}