package com.princely.shopmanager.shared.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "features.security.input-sanitization.enabled", havingValue = "true", matchIfMissing = true)
public class InputSanitizer {

    public String sanitizeForStorage(String input) {
        if (input == null) {
            return null;
        }

        // Remove potentially dangerous HTML tags and scripts
        return input.replaceAll("<script[^>]*>.*?</script>", "")
                   .replaceAll("<[^>]+>", "")
                   .trim();
    }

    public String sanitizeForSearch(String searchTerm) {
        if (searchTerm == null) {
            return null;
        }

        return searchTerm.replaceAll("[^a-zA-Z0-9\\s-]", "").trim();
    }

    public String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }

        // Basic email sanitization - remove dangerous characters
        return email.replaceAll("[<>\"']", "").trim();
    }

    public String sanitizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        // Allow only digits, spaces, dashes, and plus sign
        return phoneNumber.replaceAll("[^0-9\\s\\-\\+]", "").trim();
    }
}