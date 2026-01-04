package com.princely.shopmanager.embedded.service;

import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.embedded.dto.LoginRequest;
import com.princely.shopmanager.embedded.dto.LoginResponse;
import com.princely.shopmanager.embedded.dto.RefreshTokenRequest;
import com.princely.shopmanager.embedded.dto.RegisterRequest;
import com.princely.shopmanager.embedded.security.JwtTokenProvider;
import com.princely.shopmanager.shared.exception.BusinessException;
import com.princely.shopmanager.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmbeddedAuthService.
 * Tests authentication business logic for embedded mode (login, register, refresh token).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Embedded Auth Service Tests")
class EmbeddedAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private EmbeddedAuthService authService;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Password123!";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedPasswordHash";
    private static final String ACCESS_TOKEN = "access.token.jwt";
    private static final String REFRESH_TOKEN = "refresh.token.jwt";
    private static final long TOKEN_VALIDITY = 86400000L; // 24 hours in ms

    // ============================================================================
    // Login Tests
    // ============================================================================

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void shouldLoginSuccessfully() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername(TEST_USERNAME);
        request.setPassword(TEST_PASSWORD);

        User user = createActiveUser();
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn(ACCESS_TOKEN);
        when(jwtTokenProvider.generateRefreshToken(user)).thenReturn(REFRESH_TOKEN);
        when(jwtTokenProvider.getAccessTokenValidity()).thenReturn(TOKEN_VALIDITY);

        // When
        LoginResponse response = authService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(TOKEN_VALIDITY / 1000);

        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(passwordEncoder).matches(TEST_PASSWORD, ENCODED_PASSWORD);
        verify(jwtTokenProvider).generateAccessToken(user);
        verify(jwtTokenProvider).generateRefreshToken(user);
    }

    @Test
    @DisplayName("Should fail login when username not found")
    void shouldFailLoginWhenUsernameNotFound() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword(TEST_PASSWORD);

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);

        verify(userRepository).findByUsername("nonexistent");
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    @DisplayName("Should fail login when password is incorrect")
    void shouldFailLoginWhenPasswordIncorrect() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername(TEST_USERNAME);
        request.setPassword("WrongPassword");

        User user = createActiveUser();
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", ENCODED_PASSWORD)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);

        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(passwordEncoder).matches("WrongPassword", ENCODED_PASSWORD);
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    @DisplayName("Should fail login when user has no password hash (Keycloak user)")
    void shouldFailLoginWhenUserHasNoPasswordHash() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername(TEST_USERNAME);
        request.setPassword(TEST_PASSWORD);

        User user = createActiveUser();
        user.setPasswordHash(null); // Keycloak-managed user

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

        // When / Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);

        verify(userRepository).findByUsername(TEST_USERNAME);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    @DisplayName("Should fail login when user is inactive")
    void shouldFailLoginWhenUserInactive() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername(TEST_USERNAME);
        request.setPassword(TEST_PASSWORD);

        User user = createActiveUser();
        user.setStatus(User.UserStatus.INACTIVE);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_INACTIVE);

        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(passwordEncoder).matches(TEST_PASSWORD, ENCODED_PASSWORD);
        verifyNoInteractions(jwtTokenProvider);
    }

    // ============================================================================
    // Register Tests
    // ============================================================================

    @Test
    @DisplayName("Should successfully register new user")
    void shouldRegisterSuccessfully() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername(TEST_USERNAME);
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);
        request.setFirstName("Test");
        request.setLastName("User");

        User savedUser = createActiveUser();
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateAccessToken(savedUser)).thenReturn(ACCESS_TOKEN);
        when(jwtTokenProvider.generateRefreshToken(savedUser)).thenReturn(REFRESH_TOKEN);
        when(jwtTokenProvider.getAccessTokenValidity()).thenReturn(TOKEN_VALIDITY);

        // When
        LoginResponse response = authService.register(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(TOKEN_VALIDITY / 1000);

        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(userRepository).save(any(User.class));
        verify(jwtTokenProvider).generateAccessToken(savedUser);
        verify(jwtTokenProvider).generateRefreshToken(savedUser);
    }

    @Test
    @DisplayName("Should fail registration when username already exists")
    void shouldFailRegistrationWhenUsernameExists() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername(TEST_USERNAME);
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        User existingUser = createActiveUser();
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(existingUser));

        // When / Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USERNAME_EXISTS);

        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    @DisplayName("Should fail registration when email already exists")
    void shouldFailRegistrationWhenEmailExists() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername(TEST_USERNAME);
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        User existingUser = createActiveUser();
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingUser));

        // When / Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_EXISTS);

        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtTokenProvider);
    }

    // ============================================================================
    // Refresh Token Tests
    // ============================================================================

    @Test
    @DisplayName("Should successfully refresh token with valid refresh token")
    void shouldRefreshTokenSuccessfully() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(REFRESH_TOKEN);

        User user = createActiveUser();
        when(jwtTokenProvider.validateToken(REFRESH_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(REFRESH_TOKEN)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn(ACCESS_TOKEN);
        when(jwtTokenProvider.generateRefreshToken(user)).thenReturn(REFRESH_TOKEN);
        when(jwtTokenProvider.getAccessTokenValidity()).thenReturn(TOKEN_VALIDITY);

        // When
        LoginResponse response = authService.refreshToken(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(TOKEN_VALIDITY / 1000);

        verify(jwtTokenProvider).validateToken(REFRESH_TOKEN);
        verify(jwtTokenProvider).getUserIdFromToken(REFRESH_TOKEN);
        verify(userRepository).findById(user.getId());
        verify(jwtTokenProvider).generateAccessToken(user);
        verify(jwtTokenProvider).generateRefreshToken(user);
    }

    @Test
    @DisplayName("Should fail refresh when token is invalid")
    void shouldFailRefreshWhenTokenInvalid() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid.token");

        when(jwtTokenProvider.validateToken("invalid.token")).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOKEN_INVALID);

        verify(jwtTokenProvider).validateToken("invalid.token");
        verifyNoMoreInteractions(jwtTokenProvider);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should fail refresh when user not found")
    void shouldFailRefreshWhenUserNotFound() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(REFRESH_TOKEN);

        when(jwtTokenProvider.validateToken(REFRESH_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(REFRESH_TOKEN)).thenReturn("nonexistent-user");
        when(userRepository.findById("nonexistent-user")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(jwtTokenProvider).validateToken(REFRESH_TOKEN);
        verify(jwtTokenProvider).getUserIdFromToken(REFRESH_TOKEN);
        verify(userRepository).findById("nonexistent-user");
    }

    @Test
    @DisplayName("Should fail refresh when user is inactive")
    void shouldFailRefreshWhenUserInactive() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(REFRESH_TOKEN);

        User user = createActiveUser();
        user.setStatus(User.UserStatus.INACTIVE);

        when(jwtTokenProvider.validateToken(REFRESH_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(REFRESH_TOKEN)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // When / Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_INACTIVE);

        verify(jwtTokenProvider).validateToken(REFRESH_TOKEN);
        verify(jwtTokenProvider).getUserIdFromToken(REFRESH_TOKEN);
        verify(userRepository).findById(user.getId());
        verify(jwtTokenProvider, never()).generateAccessToken(any());
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private User createActiveUser() {
        User user = new User();
        user.setId("user-123");
        user.setUsername(TEST_USERNAME);
        user.setEmail(TEST_EMAIL);
        user.setPasswordHash(ENCODED_PASSWORD);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setStatus(User.UserStatus.ACTIVE);
        user.setRoles(new HashSet<>());
        return user;
    }
}
