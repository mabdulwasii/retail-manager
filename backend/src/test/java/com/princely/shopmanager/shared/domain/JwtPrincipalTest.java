package com.princely.shopmanager.shared.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtPrincipalTest {

    private Jwt mockJwt;
    private Map<String, Object> claims;
    private Instant now;
    private Instant futureExpiry;
    private Instant pastExpiry;

    @BeforeEach
    void setUp() {
        now = Instant.now();
        futureExpiry = now.plusSeconds(3600); // 1 hour from now
        pastExpiry = now.minusSeconds(3600);  // 1 hour ago

        claims = new HashMap<>();
        claims.put("sub", "user-123");
        claims.put("preferred_username", "johndoe");
        claims.put("tenant_id", "tenant-456");
        claims.put("email", "john.doe@example.com");
        claims.put("given_name", "John");
        claims.put("family_name", "Doe");
        claims.put("iss", "https://auth.example.com");
        claims.put("session_state", "session-789");
        claims.put("scope", "openid profile");
        claims.put("custom_claim", "custom_value");
        claims.put("numeric_claim", 123);

        mockJwt = mock(Jwt.class);
        when(mockJwt.getClaimAsString("sub")).thenReturn("user-123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("johndoe");
        when(mockJwt.getClaimAsString("tenant_id")).thenReturn("tenant-456");
        when(mockJwt.getClaimAsString("email")).thenReturn("john.doe@example.com");
        when(mockJwt.getClaimAsString("given_name")).thenReturn("John");
        when(mockJwt.getClaimAsString("family_name")).thenReturn("Doe");
        when(mockJwt.getClaimAsString("iss")).thenReturn("https://auth.example.com");
        when(mockJwt.getClaimAsString("session_state")).thenReturn("session-789");
        when(mockJwt.getClaimAsString("scope")).thenReturn("openid profile");
        when(mockJwt.getClaims()).thenReturn(claims);
        when(mockJwt.getIssuedAt()).thenReturn(now);
        when(mockJwt.getExpiresAt()).thenReturn(futureExpiry);
    }

    @Test
    void fromJwt_ShouldCreateJwtPrincipalFromJwt() {
        // Act
        JwtPrincipal principal = JwtPrincipal.fromJwt(mockJwt);

        // Assert
        assertThat(principal.getSubject()).isEqualTo("user-123");
        assertThat(principal.getPreferredUsername()).isEqualTo("johndoe");
        assertThat(principal.getTenantId()).isEqualTo("tenant-456");
        assertThat(principal.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(principal.getFirstName()).isEqualTo("John");
        assertThat(principal.getLastName()).isEqualTo("Doe");
        assertThat(principal.getIssuer()).isEqualTo("https://auth.example.com");
        assertThat(principal.getSessionState()).isEqualTo("session-789");
        assertThat(principal.getScope()).isEqualTo("openid profile");
        assertThat(principal.getClaims()).isEqualTo(claims);
        assertThat(principal.getIssuedAt()).isEqualTo(now);
        assertThat(principal.getExpiresAt()).isEqualTo(futureExpiry);
    }

    @Test
    void fromJwt_WithNullClaims_ShouldHandleGracefully() {
        // Arrange
        when(mockJwt.getClaimAsString(anyString())).thenReturn(null);
        when(mockJwt.getClaims()).thenReturn(null);
        when(mockJwt.getIssuedAt()).thenReturn(null);
        when(mockJwt.getExpiresAt()).thenReturn(null);

        // Act
        JwtPrincipal principal = JwtPrincipal.fromJwt(mockJwt);

        // Assert
        assertThat(principal.getSubject()).isNull();
        assertThat(principal.getPreferredUsername()).isNull();
        assertThat(principal.getTenantId()).isNull();
        assertThat(principal.getEmail()).isNull();
        assertThat(principal.getFirstName()).isNull();
        assertThat(principal.getLastName()).isNull();
        assertThat(principal.getClaims()).isNull();
        assertThat(principal.getIssuedAt()).isNull();
        assertThat(principal.getExpiresAt()).isNull();
    }

    @Test
    void getFullName_WithFirstAndLastName_ShouldReturnFullName() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .firstName("John")
            .lastName("Doe")
            .preferredUsername("johndoe")
            .email("john@example.com")
            .build();

        // Act
        String fullName = principal.getFullName();

        // Assert
        assertThat(fullName).isEqualTo("John Doe");
    }

    @Test
    void getFullName_WithFirstNameOnly_ShouldReturnFirstName() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .firstName("John")
            .lastName(null)
            .preferredUsername("johndoe")
            .email("john@example.com")
            .build();

        // Act
        String fullName = principal.getFullName();

        // Assert
        assertThat(fullName).isEqualTo("John");
    }

    @Test
    void getFullName_WithLastNameOnly_ShouldReturnLastName() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .firstName(null)
            .lastName("Doe")
            .preferredUsername("johndoe")
            .email("john@example.com")
            .build();

        // Act
        String fullName = principal.getFullName();

        // Assert
        assertThat(fullName).isEqualTo("Doe");
    }

    @Test
    void getFullName_WithNoNames_ShouldReturnPreferredUsername() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .firstName(null)
            .lastName(null)
            .preferredUsername("johndoe")
            .email("john@example.com")
            .build();

        // Act
        String fullName = principal.getFullName();

        // Assert
        assertThat(fullName).isEqualTo("johndoe");
    }

    @Test
    void getFullName_WithNoNamesButEmail_ShouldReturnEmail() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .firstName(null)
            .lastName(null)
            .preferredUsername(null)
            .email("john@example.com")
            .build();

        // Act
        String fullName = principal.getFullName();

        // Assert
        assertThat(fullName).isEqualTo("john@example.com");
    }

    @Test
    void getFullName_WithAllNamesNull_ShouldReturnNull() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .firstName(null)
            .lastName(null)
            .preferredUsername(null)
            .email(null)
            .build();

        // Act
        String fullName = principal.getFullName();

        // Assert
        assertThat(fullName).isNull();
    }

    @Test
    void isValid_WithValidToken_ShouldReturnTrue() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .subject("user-123")
            .expiresAt(futureExpiry)
            .build();

        // Act
        boolean isValid = principal.isValid();

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    void isValid_WithExpiredToken_ShouldReturnFalse() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .subject("user-123")
            .expiresAt(pastExpiry)
            .build();

        // Act
        boolean isValid = principal.isValid();

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void isValid_WithNullSubject_ShouldReturnFalse() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .subject(null)
            .expiresAt(futureExpiry)
            .build();

        // Act
        boolean isValid = principal.isValid();

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void isValid_WithNullExpiresAt_ShouldReturnFalse() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .subject("user-123")
            .expiresAt(null)
            .build();

        // Act
        boolean isValid = principal.isValid();

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void hasTenant_WithValidTenantId_ShouldReturnTrue() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .tenantId("tenant-456")
            .build();

        // Act
        boolean hasTenant = principal.hasTenant();

        // Assert
        assertThat(hasTenant).isTrue();
    }

    @Test
    void hasTenant_WithNullTenantId_ShouldReturnFalse() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .tenantId(null)
            .build();

        // Act
        boolean hasTenant = principal.hasTenant();

        // Assert
        assertThat(hasTenant).isFalse();
    }

    @Test
    void hasTenant_WithEmptyTenantId_ShouldReturnFalse() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .tenantId("")
            .build();

        // Act
        boolean hasTenant = principal.hasTenant();

        // Assert
        assertThat(hasTenant).isFalse();
    }

    @Test
    void hasTenant_WithWhitespaceTenantId_ShouldReturnFalse() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .tenantId("   ")
            .build();

        // Act
        boolean hasTenant = principal.hasTenant();

        // Assert
        assertThat(hasTenant).isFalse();
    }

    @Test
    void getClaim_WithValidClaim_ShouldReturnValue() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .claims(claims)
            .build();

        // Act
        String stringClaim = principal.getClaim("custom_claim", String.class);
        Integer numericClaim = principal.getClaim("numeric_claim", Integer.class);

        // Assert
        assertThat(stringClaim).isEqualTo("custom_value");
        assertThat(numericClaim).isEqualTo(123);
    }

    @Test
    void getClaim_WithNullClaims_ShouldReturnNull() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .claims(null)
            .build();

        // Act
        String claim = principal.getClaim("custom_claim", String.class);

        // Assert
        assertThat(claim).isNull();
    }

    @Test
    void getClaim_WithNonExistentClaim_ShouldReturnNull() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .claims(claims)
            .build();

        // Act
        String claim = principal.getClaim("non_existent", String.class);

        // Assert
        assertThat(claim).isNull();
    }

    @Test
    void getClaim_WithWrongType_ShouldReturnNull() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .claims(claims)
            .build();

        // Act
        Integer claim = principal.getClaim("custom_claim", Integer.class);

        // Assert
        assertThat(claim).isNull();
    }

    @Test
    void getClaimAsString_WithValidClaim_ShouldReturnStringValue() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .claims(claims)
            .build();

        // Act
        String claim = principal.getClaimAsString("custom_claim");

        // Assert
        assertThat(claim).isEqualTo("custom_value");
    }

    @Test
    void getClaimAsString_WithNonStringClaim_ShouldReturnNull() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .claims(claims)
            .build();

        // Act
        String claim = principal.getClaimAsString("numeric_claim");

        // Assert
        assertThat(claim).isNull();
    }

    @Test
    void hasRole_WithValidRole_ShouldReturnTrue() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .roles(Arrays.asList("admin", "user", "manager"))
            .build();

        // Act
        boolean hasAdmin = principal.hasRole("admin");
        boolean hasUser = principal.hasRole("user");

        // Assert
        assertThat(hasAdmin).isTrue();
        assertThat(hasUser).isTrue();
    }

    @Test
    void hasRole_WithInvalidRole_ShouldReturnFalse() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .roles(Arrays.asList("admin", "user"))
            .build();

        // Act
        boolean hasRole = principal.hasRole("manager");

        // Assert
        assertThat(hasRole).isFalse();
    }

    @Test
    void hasRole_WithNullRoles_ShouldReturnFalse() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .roles(null)
            .build();

        // Act
        boolean hasRole = principal.hasRole("admin");

        // Assert
        assertThat(hasRole).isFalse();
    }

    @Test
    void hasRole_WithEmptyRoles_ShouldReturnFalse() {
        // Arrange
        JwtPrincipal principal = JwtPrincipal.builder()
            .roles(Arrays.asList())
            .build();

        // Act
        boolean hasRole = principal.hasRole("admin");

        // Assert
        assertThat(hasRole).isFalse();
    }
}