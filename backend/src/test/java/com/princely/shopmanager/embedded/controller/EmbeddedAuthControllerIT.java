package com.princely.shopmanager.embedded.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.embedded.dto.LoginRequest;
import com.princely.shopmanager.embedded.dto.LoginResponse;
import com.princely.shopmanager.embedded.dto.RefreshTokenRequest;
import com.princely.shopmanager.embedded.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for EmbeddedAuthController.
 * Tests authentication endpoints with real Spring Boot context in embedded mode.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("embedded")
@org.springframework.test.context.TestPropertySource(properties = {
    "app.keycloak.enabled=false",
    "app.update-check.enabled=true",
    "embedded.postgres.data-dir=./target/test-postgres-auth",
    "embedded.postgres.port=5435"
})
@Transactional
@DisplayName("Embedded Auth Controller - Integration Tests")
class EmbeddedAuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String AUTH_BASE_URL = "/api/auth";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Password123!";

    @BeforeEach
    void setUp() {
        // Clean up test data
        userRepository.deleteAll();
    }

    // ============================================================================
    // POST /api/auth/login Tests
    // ============================================================================

    @Test
    @DisplayName("POST /api/auth/login - Should return 200 with tokens for valid credentials")
    void shouldLoginSuccessfully() throws Exception {
        // Given
        createTestUser(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD);

        LoginRequest request = new LoginRequest();
        request.setUsername(TEST_USERNAME);
        request.setPassword(TEST_PASSWORD);

        // When / Then
        MvcResult result = mockMvc.perform(post(AUTH_BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        LoginResponse response = objectMapper.readValue(responseBody, LoginResponse.class);

        assertThat(response.getAccessToken()).isNotEmpty();
        assertThat(response.getRefreshToken()).isNotEmpty();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isGreaterThan(0);
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 401 for invalid username")
    void shouldFailLoginWithInvalidUsername() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword(TEST_PASSWORD);

        // When / Then
        mockMvc.perform(post(AUTH_BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 401 for invalid password")
    void shouldFailLoginWithInvalidPassword() throws Exception {
        // Given
        createTestUser(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD);

        LoginRequest request = new LoginRequest();
        request.setUsername(TEST_USERNAME);
        request.setPassword("WrongPassword!");

        // When / Then
        mockMvc.perform(post(AUTH_BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 400 for missing username")
    void shouldFailLoginWithMissingUsername() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setPassword(TEST_PASSWORD);
        // username is null

        // When / Then
        mockMvc.perform(post(AUTH_BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 400 for missing password")
    void shouldFailLoginWithMissingPassword() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername(TEST_USERNAME);
        // password is null

        // When / Then
        mockMvc.perform(post(AUTH_BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ============================================================================
    // POST /api/auth/register Tests
    // ============================================================================

    @Test
    @DisplayName("POST /api/auth/register - Should return 200 with tokens for valid registration")
    void shouldRegisterSuccessfully() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("SecurePass123!");
        request.setFirstName("New");
        request.setLastName("User");

        // When / Then
        MvcResult result = mockMvc.perform(post(AUTH_BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        LoginResponse response = objectMapper.readValue(responseBody, LoginResponse.class);

        assertThat(response.getAccessToken()).isNotEmpty();
        assertThat(response.getRefreshToken()).isNotEmpty();

        // Verify user was saved to database
        User savedUser = userRepository.findByUsername("newuser").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
        assertThat(savedUser.getFirstName()).isEqualTo("New");
        assertThat(savedUser.getLastName()).isEqualTo("User");
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 409 when username already exists")
    void shouldFailRegistrationWhenUsernameExists() throws Exception {
        // Given
        createTestUser(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD);

        RegisterRequest request = new RegisterRequest();
        request.setUsername(TEST_USERNAME); // Duplicate username
        request.setEmail("different@example.com");
        request.setPassword("SecurePass123!");

        // When / Then
        mockMvc.perform(post(AUTH_BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 409 when email already exists")
    void shouldFailRegistrationWhenEmailExists() throws Exception {
        // Given
        createTestUser(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("differentuser");
        request.setEmail(TEST_EMAIL); // Duplicate email
        request.setPassword("SecurePass123!");

        // When / Then
        mockMvc.perform(post(AUTH_BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 for invalid email format")
    void shouldFailRegistrationWithInvalidEmail() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("invalid-email"); // Invalid email format
        request.setPassword("SecurePass123!");

        // When / Then
        mockMvc.perform(post(AUTH_BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 for short password")
    void shouldFailRegistrationWithShortPassword() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("short"); // Less than 8 characters

        // When / Then
        mockMvc.perform(post(AUTH_BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ============================================================================
    // POST /api/auth/refresh Tests
    // ============================================================================

    @Test
    @DisplayName("POST /api/auth/refresh - Should return 200 with new tokens for valid refresh token")
    void shouldRefreshTokenSuccessfully() throws Exception {
        // Given
        createTestUser(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD);

        // First login to get tokens
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);

        MvcResult loginResult = mockMvc.perform(post(AUTH_BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseBody = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(loginResponseBody, LoginResponse.class);
        String refreshToken = loginResponse.getRefreshToken();

        // Now use refresh token
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(refreshToken);

        // When / Then
        MvcResult refreshResult = mockMvc.perform(post(AUTH_BASE_URL + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        String refreshResponseBody = refreshResult.getResponse().getContentAsString();
        LoginResponse refreshResponse = objectMapper.readValue(refreshResponseBody, LoginResponse.class);

        assertThat(refreshResponse.getAccessToken()).isNotEmpty();
        assertThat(refreshResponse.getRefreshToken()).isNotEmpty();
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Should return 401 for invalid refresh token")
    void shouldFailRefreshWithInvalidToken() throws Exception {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid.jwt.token");

        // When / Then
        mockMvc.perform(post(AUTH_BASE_URL + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Should return 400 for missing refresh token")
    void shouldFailRefreshWithMissingToken() throws Exception {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        // refreshToken is null

        // When / Then
        mockMvc.perform(post(AUTH_BASE_URL + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private User createTestUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhoneNumber("+1234567890"); // Required field
        user.setStatus(User.UserStatus.ACTIVE);
        user.setRoles(new HashSet<>());
        return userRepository.save(user);
    }
}
