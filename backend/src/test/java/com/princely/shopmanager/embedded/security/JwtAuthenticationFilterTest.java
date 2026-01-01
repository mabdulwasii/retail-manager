package com.princely.shopmanager.embedded.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter.
 * Tests JWT token extraction, validation, and security context setup.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Authentication Filter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String INVALID_TOKEN = "invalid.token";
    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider);
        // Clear security context before each test
        SecurityContextHolder.clearContext();
    }

    // ============================================================================
    // Valid Token Tests
    // ============================================================================

    @Test
    @DisplayName("Should set authentication in SecurityContext with valid Bearer token")
    void shouldSetAuthenticationWithValidToken() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer " + VALID_TOKEN;
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(VALID_TOKEN)).thenReturn(TEST_USERNAME);
        when(jwtTokenProvider.getRolesFromToken(VALID_TOKEN)).thenReturn(roles);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(TEST_USERNAME);
        assertThat(authentication.getAuthorities())
                .hasSize(2)
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");

        verify(jwtTokenProvider).validateToken(VALID_TOKEN);
        verify(jwtTokenProvider).getUsernameFromToken(VALID_TOKEN);
        verify(jwtTokenProvider).getRolesFromToken(VALID_TOKEN);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should extract username and roles correctly from token")
    void shouldExtractUsernameAndRolesCorrectly() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer " + VALID_TOKEN;
        List<String> roles = List.of("ROLE_MANAGER");

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(VALID_TOKEN)).thenReturn("manager");
        when(jwtTokenProvider.getRolesFromToken(VALID_TOKEN)).thenReturn(roles);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.getPrincipal()).isEqualTo("manager");
        assertThat(authentication.getAuthorities())
                .hasSize(1)
                .extracting("authority")
                .containsExactly("ROLE_MANAGER");

        verify(filterChain).doFilter(request, response);
    }

    // ============================================================================
    // Missing/Invalid Token Tests
    // ============================================================================

    @Test
    @DisplayName("Should not set authentication when Authorization header is missing")
    void shouldNotSetAuthenticationWhenHeaderMissing() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(request).getHeader("Authorization");
        verifyNoInteractions(jwtTokenProvider);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not set authentication when Authorization header is empty")
    void shouldNotSetAuthenticationWhenHeaderEmpty() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verifyNoInteractions(jwtTokenProvider);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not set authentication when Bearer prefix is missing")
    void shouldNotSetAuthenticationWithoutBearerPrefix() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(VALID_TOKEN); // No "Bearer " prefix

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verifyNoInteractions(jwtTokenProvider);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not set authentication with Basic auth scheme")
    void shouldNotSetAuthenticationWithBasicAuth() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNzd29yZA==");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verifyNoInteractions(jwtTokenProvider);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not set authentication when token validation fails")
    void shouldNotSetAuthenticationWhenTokenInvalid() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer " + INVALID_TOKEN;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtTokenProvider.validateToken(INVALID_TOKEN)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(jwtTokenProvider).validateToken(INVALID_TOKEN);
        verify(jwtTokenProvider, never()).getUsernameFromToken(anyString());
        verify(jwtTokenProvider, never()).getRolesFromToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    // ============================================================================
    // Exception Handling Tests
    // ============================================================================

    @Test
    @DisplayName("Should handle token provider exceptions gracefully and continue filter chain")
    void shouldHandleExceptionsGracefully() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer " + VALID_TOKEN;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenThrow(new RuntimeException("Token parsing error"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(jwtTokenProvider).validateToken(VALID_TOKEN);
        verify(filterChain).doFilter(request, response); // Should still call filter chain
    }

    @Test
    @DisplayName("Should always call filterChain.doFilter regardless of token validity")
    void shouldAlwaysCallFilterChain() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
    }

    // ============================================================================
    // Edge Cases
    // ============================================================================

    @Test
    @DisplayName("Should handle Bearer token with trailing spaces")
    void shouldHandleBearerTokenWithSpaces() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer   " + VALID_TOKEN + "   "; // Extra spaces
        List<String> roles = List.of("ROLE_USER");

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        // The substring(7) will extract everything after "Bearer "
        String extractedToken = authHeader.substring(7);
        when(jwtTokenProvider.validateToken(extractedToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(extractedToken)).thenReturn(TEST_USERNAME);
        when(jwtTokenProvider.getRolesFromToken(extractedToken)).thenReturn(roles);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(TEST_USERNAME);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should handle token with no roles")
    void shouldHandleTokenWithNoRoles() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer " + VALID_TOKEN;
        List<String> emptyRoles = List.of();

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(VALID_TOKEN)).thenReturn(TEST_USERNAME);
        when(jwtTokenProvider.getRolesFromToken(VALID_TOKEN)).thenReturn(emptyRoles);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(TEST_USERNAME);
        assertThat(authentication.getAuthorities()).isEmpty();

        verify(filterChain).doFilter(request, response);
    }
}
