package com.princely.shopmanager.embedded.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JWT Token Provider Tests")
@Execution(ExecutionMode.SAME_THREAD) // JJWT ServiceLoader is not thread-safe
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_SECRET = "testSecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLongToBeSecure";
    private static final long JWT_EXPIRATION = 86400000; // 24 hours
    private static final long REFRESH_EXPIRATION = 604800000; // 7 days
    private static final String ISSUER = "shop-manager-test";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                TEST_SECRET,
                JWT_EXPIRATION,
                REFRESH_EXPIRATION,
                ISSUER
        );
    }

    @Test
    @DisplayName("Should generate valid access token")
    void shouldGenerateValidAccessToken() {
        // Given
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password",
                authorities
        );

        // When
        String token = jwtTokenProvider.generateToken(authentication);

        // Then
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsernameFromToken() {
        // Given
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password",
                authorities
        );
        String token = jwtTokenProvider.generateToken(authentication);

        // When
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should extract roles from token")
    void shouldExtractRolesFromToken() {
        // Given
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password",
                authorities
        );
        String token = jwtTokenProvider.generateToken(authentication);

        // When
        List<String> roles = jwtTokenProvider.getRolesFromToken(token);

        // Then
        assertThat(roles).hasSize(2).contains("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should generate valid refresh token")
    void shouldGenerateValidRefreshToken() {
        // Given
        String username = "testuser";

        // When
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        // Then
        assertThat(refreshToken).isNotNull().isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
    }

    @Test
    @DisplayName("Should extract username from refresh token")
    void shouldExtractUsernameFromRefreshToken() {
        // Given
        String username = "testuser";
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        // When
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // Then
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("Should validate token successfully")
    void shouldValidateTokenSuccessfully() {
        // Given
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password",
                authorities
        );
        String token = jwtTokenProvider.generateToken(authentication);

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should fail validation for invalid token")
    void shouldFailValidationForInvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should fail validation for malformed token")
    void shouldFailValidationForMalformedToken() {
        // Given
        String malformedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.malformed";

        // When
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        // Given
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication auth1 = new UsernamePasswordAuthenticationToken("user1", "password", authorities);
        Authentication auth2 = new UsernamePasswordAuthenticationToken("user2", "password", authorities);

        // When
        String token1 = jwtTokenProvider.generateToken(auth1);
        String token2 = jwtTokenProvider.generateToken(auth2);

        // Then
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtTokenProvider.getUsernameFromToken(token1)).isEqualTo("user1");
        assertThat(jwtTokenProvider.getUsernameFromToken(token2)).isEqualTo("user2");
    }

    @Test
    @DisplayName("Should handle multiple roles correctly")
    void shouldHandleMultipleRolesCorrectly() {
        // Given
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_MANAGER")
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password",
                authorities
        );

        // When
        String token = jwtTokenProvider.generateToken(authentication);
        List<String> roles = jwtTokenProvider.getRolesFromToken(token);

        // Then
        assertThat(roles).hasSize(3).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER");
    }
}
