package com.princely.shopmanager.auth.security;

import com.princely.shopmanager.shared.domain.JwtPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CustomJwtAuthenticationToken Tests")
class CustomJwtAuthenticationTokenTest {

    private Jwt jwt;
    private JwtPrincipal principal;
    private List<GrantedAuthority> authorities;

    @BeforeEach
    void setUp() {
        jwt = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claim("sub", "user123")
            .claim("preferred_username", "testuser")
            .claim("email", "test@example.com")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        principal = JwtPrincipal.builder()
            .userId("user123")
            .preferredUsername("testuser")
            .email("test@example.com")
            .tenantId("tenant-1")
            .shopId("shop-1")
            .roles(List.of("ROLE_USER", "ROLE_ADMIN"))
            .build();

        authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
    }

    @Test
    @DisplayName("Should create token with JwtPrincipal")
    void shouldCreateTokenWithJwtPrincipal() {
        // When
        CustomJwtAuthenticationToken token = new CustomJwtAuthenticationToken(jwt, authorities, principal);

        // Then
        assertThat(token).isNotNull();
        assertThat(token.getJwtPrincipal()).isEqualTo(principal);
        assertThat(token.getPrincipal()).isEqualTo(principal);
        assertThat(token.getAuthorities()).containsExactlyInAnyOrderElementsOf(authorities);
        assertThat(token.getToken()).isEqualTo(jwt);
    }

    @Test
    @DisplayName("Should return JwtPrincipal from getPrincipal()")
    void shouldReturnJwtPrincipalFromGetPrincipal() {
        // Given
        CustomJwtAuthenticationToken token = new CustomJwtAuthenticationToken(jwt, authorities, principal);

        // When
        Object result = token.getPrincipal();

        // Then
        assertThat(result).isInstanceOf(JwtPrincipal.class)
                .isEqualTo(principal);
    }

    @Test
    @DisplayName("Should preserve JWT token")
    void shouldPreserveJwtToken() {
        // Given
        CustomJwtAuthenticationToken token = new CustomJwtAuthenticationToken(jwt, authorities, principal);

        // When/Then
        assertThat(token.getToken()).isEqualTo(jwt);
        assertThat(token.getTokenAttributes()).isEqualTo(jwt.getClaims());
    }

    @Test
    @DisplayName("Should use preferredUsername as name")
    void shouldUsePreferredUsernameAsName() {
        // Given
        CustomJwtAuthenticationToken token = new CustomJwtAuthenticationToken(jwt, authorities, principal);

        // When
        String name = token.getName();

        // Then
        assertThat(name).isEqualTo("testuser")
                .isEqualTo(principal.getPreferredUsername());
    }

    @Test
    @DisplayName("Should be authenticated by default")
    void shouldBeAuthenticatedByDefault() {
        // Given
        CustomJwtAuthenticationToken token = new CustomJwtAuthenticationToken(jwt, authorities, principal);

        // When/Then
        assertThat(token.isAuthenticated()).isTrue();
    }

    @Test
    @DisplayName("Should handle empty authorities")
    void shouldHandleEmptyAuthorities() {
        // Given
        List<GrantedAuthority> emptyAuthorities = List.of();

        // When
        CustomJwtAuthenticationToken token = new CustomJwtAuthenticationToken(jwt, emptyAuthorities, principal);

        // Then
        assertThat(token.getAuthorities()).isEmpty();
        assertThat(token.getPrincipal()).isEqualTo(principal);
    }

    @Test
    @DisplayName("Should expose JwtPrincipal fields correctly")
    void shouldExposeJwtPrincipalFieldsCorrectly() {
        // Given
        CustomJwtAuthenticationToken token = new CustomJwtAuthenticationToken(jwt, authorities, principal);

        // When
        JwtPrincipal extractedPrincipal = token.getJwtPrincipal();

        // Then
        assertThat(extractedPrincipal.getUserId()).isEqualTo("user123");
        assertThat(extractedPrincipal.getEmail()).isEqualTo("test@example.com");
        assertThat(extractedPrincipal.getTenantId()).isEqualTo("tenant-1");
        assertThat(extractedPrincipal.getShopId()).isEqualTo("shop-1");
        assertThat(extractedPrincipal.getRoles()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }
}
