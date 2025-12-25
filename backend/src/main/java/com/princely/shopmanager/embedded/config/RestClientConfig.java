package com.princely.shopmanager.embedded.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * RestClient configuration for embedded mode cloud sync.
 * RestClient is the modern replacement for RestTemplate in Spring Boot 3.x+
 */
@Configuration
@Profile("embedded")
public class RestClientConfig {

    /**
     * Create RestClient bean for cloud sync HTTP communication.
     * Configured with sensible defaults for production use.
     */
    @Bean
    public RestClient cloudSyncRestClient(RestClient.Builder builder) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000); // 10 seconds
        requestFactory.setReadTimeout(30000);    // 30 seconds

        return builder
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.USER_AGENT, "ShopManager-Embedded/1.0")
                .build();
    }
}
