package com.princely.shopmanager.shared.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@ConditionalOnProperty(name = "features.async.enabled", havingValue = "true", matchIfMissing = true)
public class AsyncConfig {

    @Bean(name = "businessTaskExecutor")
    public Executor businessTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("business-async-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "analyticsTaskExecutor")
    public Executor analyticsTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("analytics-async-");
        executor.initialize();
        return executor;
    }
}