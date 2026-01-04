package com.princely.shopmanager.embedded.service;

import com.princely.shopmanager.auth.dto.CreateKeycloakUserRequest;
import com.princely.shopmanager.auth.service.UserManagementService;
import com.princely.shopmanager.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Embedded mode implementation of UserManagementService.
 *
 * <p>This implementation provides user management for standalone embedded
 * deployments without Keycloak. It uses local database for user storage.
 *
 * <p>Marked as @Primary to take precedence over mocked KeycloakUserService
 * in integration tests when embedded profile is active.
 */
@Service
@Profile("embedded")
@Primary
@RequiredArgsConstructor
@Slf4j
public class EmbeddedUserManagementService implements UserManagementService {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARS;
    private static final int PASSWORD_LENGTH = 16;

    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    @Override
    public String generatePassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        // Ensure at least one character from each category
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));

        // Fill remaining characters
        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    @Override
    public String createUser(CreateKeycloakUserRequest request) {
        // In embedded mode, user creation is handled by TenantRegistrationService
        // This method just returns a UUID as a placeholder ID
        log.debug("Embedded mode: Skipping external user creation for: {}", request.email());
        return UUID.randomUUID().toString();
    }

    @Override
    public void updateUserStatus(String userId, boolean enabled) {
        // In embedded mode, user status is managed directly in the database
        // No external system update needed
        log.debug("Embedded mode: Skipping external user status update for: {} (enabled: {})", userId, enabled);
    }

    @Override
    public boolean userExistsByEmail(String email) {
        // Check only local database in embedded mode
        return userRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean userExistsByUsername(String username) {
        // Check only local database in embedded mode
        return userRepository.existsByUsernameIgnoreCase(username);
    }
}
