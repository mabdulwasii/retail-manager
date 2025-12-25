package com.princely.shopmanager.embedded.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * Encryption configuration for embedded mode.
 * Provides encryption for sensitive data like API keys.
 */
@Configuration
@Profile("embedded")
@Slf4j
public class EncryptionConfig {

    @Value("${application.encryption.secret:changeme-encryption-secret-key}")
    private String encryptionSecret;

    @Value("${application.encryption.salt:deadbeef}")
    private String encryptionSalt;

    /**
     * Text encryptor for encrypting sensitive configuration data
     */
    @Bean
    public TextEncryptor textEncryptor() {
        if ("changeme-encryption-secret-key".equals(encryptionSecret)) {
            log.warn("Using default encryption secret! Please configure application.encryption.secret " +
                    "in production for security.");
        }

        return Encryptors.text(encryptionSecret, encryptionSalt);
    }
}
