package com.princely.shopmanager.embedded.service;

import com.princely.shopmanager.auth.dto.CreateKeycloakUserRequest;
import com.princely.shopmanager.core.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmbeddedUserManagementService.
 * Tests user management operations for embedded mode (without Keycloak).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Embedded User Management Service Tests")
class EmbeddedUserManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmbeddedUserManagementService userManagementService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "testuser";

    // ============================================================================
    // Password Generation Tests
    // ============================================================================

    @Test
    @DisplayName("Should generate password with exactly 16 characters")
    void shouldGeneratePasswordWith16Characters() {
        // When
        String password = userManagementService.generatePassword();

        // Then
        assertThat(password).hasSize(16);
    }

    @Test
    @DisplayName("Should generate password containing uppercase letter")
    void shouldGeneratePasswordWithUppercase() {
        // When
        String password = userManagementService.generatePassword();

        // Then
        assertThat(password).matches(".*[A-Z].*");
    }

    @Test
    @DisplayName("Should generate password containing lowercase letter")
    void shouldGeneratePasswordWithLowercase() {
        // When
        String password = userManagementService.generatePassword();

        // Then
        assertThat(password).matches(".*[a-z].*");
    }

    @Test
    @DisplayName("Should generate password containing digit")
    void shouldGeneratePasswordWithDigit() {
        // When
        String password = userManagementService.generatePassword();

        // Then
        assertThat(password).matches(".*[0-9].*");
    }

    @Test
    @DisplayName("Should generate password containing special character")
    void shouldGeneratePasswordWithSpecialChar() {
        // When
        String password = userManagementService.generatePassword();

        // Then
        assertThat(password).matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*");
    }

    @Test
    @DisplayName("Should generate password with all required character types")
    void shouldGeneratePasswordWithAllCharacterTypes() {
        // When
        String password = userManagementService.generatePassword();

        // Then
        assertThat(password)
                .hasSize(16)
                .matches(".*[A-Z].*")  // uppercase
                .matches(".*[a-z].*")  // lowercase
                .matches(".*[0-9].*")  // digit
                .matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*"); // special
    }

    @RepeatedTest(10)
    @DisplayName("Should generate different passwords on each call")
    void shouldGenerateDifferentPasswords() {
        // Given
        Set<String> passwords = new HashSet<>();

        // When - Generate passwords multiple times
        for (int i = 0; i < 10; i++) {
            passwords.add(userManagementService.generatePassword());
        }

        // Then - All passwords should be unique (with very high probability)
        assertThat(passwords).hasSizeGreaterThanOrEqualTo(8); // Allow for rare collisions
    }

    @Test
    @DisplayName("Should generate secure random password")
    void shouldGenerateSecureRandomPassword() {
        // Given
        Set<String> passwords = new HashSet<>();
        int iterations = 100;

        // When - Generate many passwords
        for (int i = 0; i < iterations; i++) {
            passwords.add(userManagementService.generatePassword());
        }

        // Then - Should have high uniqueness rate (> 95%)
        assertThat(passwords.size()).isGreaterThan(95);
    }

    // ============================================================================
    // Create User Tests
    // ============================================================================

    @Test
    @DisplayName("Should return valid UUID when creating user")
    void shouldReturnValidUuidWhenCreatingUser() {
        // Given
        CreateKeycloakUserRequest request = new CreateKeycloakUserRequest(
                TEST_USERNAME,
                TEST_EMAIL,
                "Test",
                "User",
                "+1234567890",
                "tenant-123",
                "shop-456",
                "password123",
                false,
                true,
                List.of("USER")
        );

        // When
        String userId = userManagementService.createUser(request);

        // Then
        assertThat(userId).isNotNull();
        assertThatNoException().isThrownBy(() -> UUID.fromString(userId));
    }

    @Test
    @DisplayName("Should return different UUIDs for different user creation calls")
    void shouldReturnDifferentUuidsForDifferentCalls() {
        // Given
        CreateKeycloakUserRequest request = new CreateKeycloakUserRequest(
                TEST_USERNAME,
                TEST_EMAIL,
                "Test",
                "User",
                "+1234567890",
                "tenant-123",
                "shop-456",
                "password123",
                false,
                true,
                List.of("USER")
        );

        // When
        String userId1 = userManagementService.createUser(request);
        String userId2 = userManagementService.createUser(request);

        // Then
        assertThat(userId1).isNotEqualTo(userId2);
    }

    @Test
    @DisplayName("Should not throw exception when creating user")
    void shouldNotThrowExceptionWhenCreatingUser() {
        // Given
        CreateKeycloakUserRequest request = new CreateKeycloakUserRequest(
                TEST_USERNAME,
                TEST_EMAIL,
                "Test",
                "User",
                "+1234567890",
                "tenant-123",
                "shop-456",
                "password123",
                false,
                true,
                List.of("USER")
        );

        // When / Then
        assertThatNoException().isThrownBy(() -> userManagementService.createUser(request));
    }

    // ============================================================================
    // Update User Status Tests
    // ============================================================================

    @Test
    @DisplayName("Should not throw exception when updating user status to enabled")
    void shouldNotThrowExceptionWhenEnablingUser() {
        // When / Then
        assertThatNoException().isThrownBy(() ->
                userManagementService.updateUserStatus("user-123", true));
    }

    @Test
    @DisplayName("Should not throw exception when updating user status to disabled")
    void shouldNotThrowExceptionWhenDisablingUser() {
        // When / Then
        assertThatNoException().isThrownBy(() ->
                userManagementService.updateUserStatus("user-123", false));
    }

    // ============================================================================
    // User Exists by Email Tests
    // ============================================================================

    @Test
    @DisplayName("Should return true when user exists by email")
    void shouldReturnTrueWhenUserExistsByEmail() {
        // Given
        when(userRepository.existsByEmailIgnoreCase(TEST_EMAIL)).thenReturn(true);

        // When
        boolean exists = userManagementService.userExistsByEmail(TEST_EMAIL);

        // Then
        assertThat(exists).isTrue();
        verify(userRepository).existsByEmailIgnoreCase(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should return false when user does not exist by email")
    void shouldReturnFalseWhenUserDoesNotExistByEmail() {
        // Given
        when(userRepository.existsByEmailIgnoreCase(TEST_EMAIL)).thenReturn(false);

        // When
        boolean exists = userManagementService.userExistsByEmail(TEST_EMAIL);

        // Then
        assertThat(exists).isFalse();
        verify(userRepository).existsByEmailIgnoreCase(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should check email existence case-insensitively")
    void shouldCheckEmailExistenceCaseInsensitively() {
        // Given
        String mixedCaseEmail = "Test@EXAMPLE.com";
        when(userRepository.existsByEmailIgnoreCase(mixedCaseEmail)).thenReturn(true);

        // When
        boolean exists = userManagementService.userExistsByEmail(mixedCaseEmail);

        // Then
        assertThat(exists).isTrue();
        verify(userRepository).existsByEmailIgnoreCase(mixedCaseEmail);
    }

    // ============================================================================
    // User Exists by Username Tests
    // ============================================================================

    @Test
    @DisplayName("Should return true when user exists by username")
    void shouldReturnTrueWhenUserExistsByUsername() {
        // Given
        when(userRepository.existsByUsernameIgnoreCase(TEST_USERNAME)).thenReturn(true);

        // When
        boolean exists = userManagementService.userExistsByUsername(TEST_USERNAME);

        // Then
        assertThat(exists).isTrue();
        verify(userRepository).existsByUsernameIgnoreCase(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should return false when user does not exist by username")
    void shouldReturnFalseWhenUserDoesNotExistByUsername() {
        // Given
        when(userRepository.existsByUsernameIgnoreCase(TEST_USERNAME)).thenReturn(false);

        // When
        boolean exists = userManagementService.userExistsByUsername(TEST_USERNAME);

        // Then
        assertThat(exists).isFalse();
        verify(userRepository).existsByUsernameIgnoreCase(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should check username existence case-insensitively")
    void shouldCheckUsernameExistenceCaseInsensitively() {
        // Given
        String mixedCaseUsername = "TestUser";
        when(userRepository.existsByUsernameIgnoreCase(mixedCaseUsername)).thenReturn(true);

        // When
        boolean exists = userManagementService.userExistsByUsername(mixedCaseUsername);

        // Then
        assertThat(exists).isTrue();
        verify(userRepository).existsByUsernameIgnoreCase(mixedCaseUsername);
    }

    // ============================================================================
    // Edge Cases
    // ============================================================================

    @Test
    @DisplayName("Should handle null userId in updateUserStatus gracefully")
    void shouldHandleNullUserIdInUpdateStatus() {
        // When / Then
        assertThatNoException().isThrownBy(() ->
                userManagementService.updateUserStatus(null, true));
    }

    @Test
    @DisplayName("Should handle empty userId in updateUserStatus gracefully")
    void shouldHandleEmptyUserIdInUpdateStatus() {
        // When / Then
        assertThatNoException().isThrownBy(() ->
                userManagementService.updateUserStatus("", false));
    }
}
