package com.princely.shopmanager.auth.security;

import com.princely.shopmanager.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthConverterTest {

    private JwtAuthConverter jwtAuthConverter;
    private UserRepository userRepository;
    private Jwt mockJwt;
    private Instant now;
    private Instant futureExpiry;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        jwtAuthConverter = new JwtAuthConverter(userRepository);
        ReflectionTestUtils.setField(jwtAuthConverter, "clientId", "shop-manager");

        now = Instant.now();
        futureExpiry = now.plusSeconds(3600);

        mockJwt = mock(Jwt.class);
    }

    @Test
    void convert_WithValidJwtAndRoles_ShouldReturnAuthenticationToken() {
        // Arrange
        Map<String, Object> resourceAccess = new HashMap<>();
        Map<String, Object> clientResource = new HashMap<>();
        List<String> roles = Arrays.asList("admin", "user", "manager");
        clientResource.put("roles", roles);
        resourceAccess.put("shop-manager", clientResource);

        when(mockJwt.getClaim("resource_access")).thenReturn(resourceAccess);
        when(mockJwt.getClaim("preferred_username")).thenReturn("johndoe");
        when(mockJwt.getClaimAsString("sub")).thenReturn("user-123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("johndoe");
        when(mockJwt.getClaimAsString("tenant_id")).thenReturn("tenant-456");
        when(mockJwt.getClaimAsString("email")).thenReturn("john.doe@example.com");
        when(mockJwt.getClaimAsString("given_name")).thenReturn("John");
        when(mockJwt.getClaimAsString("family_name")).thenReturn("Doe");
        when(mockJwt.getClaimAsString("iss")).thenReturn("https://auth.example.com");
        when(mockJwt.getClaimAsString("session_state")).thenReturn("session-789");
        when(mockJwt.getClaimAsString("scope")).thenReturn("openid profile");
        when(mockJwt.getClaims()).thenReturn(Map.of("sub", "user-123"));
        when(mockJwt.getIssuedAt()).thenReturn(now);
        when(mockJwt.getExpiresAt()).thenReturn(futureExpiry);

        // Act
        AbstractAuthenticationToken token = jwtAuthConverter.convert(mockJwt);

        // Assert
        assertThat(token).isInstanceOf(JwtAuthenticationToken.class);
        assertThat(token.getName()).isEqualTo("johndoe");

        Collection<GrantedAuthority> authorities = token.getAuthorities();
        assertThat(authorities).hasSize(3);
        assertThat(authorities)
            .extracting(GrantedAuthority::getAuthority)
            .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER", "ROLE_MANAGER");
    }

    @Test
    void convert_WithNullResourceAccess_ShouldReturnTokenWithoutRoles() {
        // Arrange
        when(mockJwt.getClaim("resource_access")).thenReturn(null);
        when(mockJwt.getClaim("preferred_username")).thenReturn("johndoe");
        when(mockJwt.getClaimAsString("sub")).thenReturn("user-123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("johndoe");
        when(mockJwt.getClaimAsString("tenant_id")).thenReturn("tenant-456");
        when(mockJwt.getClaimAsString("email")).thenReturn("john.doe@example.com");
        when(mockJwt.getClaims()).thenReturn(Map.of("sub", "user-123"));
        when(mockJwt.getIssuedAt()).thenReturn(now);
        when(mockJwt.getExpiresAt()).thenReturn(futureExpiry);

        // Act
        AbstractAuthenticationToken token = jwtAuthConverter.convert(mockJwt);

        // Assert
        assertThat(token).isInstanceOf(JwtAuthenticationToken.class);
        assertThat(token.getName()).isEqualTo("johndoe");
        assertThat(token.getAuthorities()).isEmpty();
    }

    @Test
    void convert_WithEmptyResourceAccess_ShouldReturnTokenWithoutRoles() {
        // Arrange
        Map<String, Object> resourceAccess = new HashMap<>();

        when(mockJwt.getClaim("resource_access")).thenReturn(resourceAccess);
        when(mockJwt.getClaim("preferred_username")).thenReturn("johndoe");
        when(mockJwt.getClaimAsString("sub")).thenReturn("user-123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("johndoe");
        when(mockJwt.getClaims()).thenReturn(Map.of("sub", "user-123"));
        when(mockJwt.getIssuedAt()).thenReturn(now);
        when(mockJwt.getExpiresAt()).thenReturn(futureExpiry);

        // Act
        AbstractAuthenticationToken token = jwtAuthConverter.convert(mockJwt);

        // Assert
        assertThat(token).isInstanceOf(JwtAuthenticationToken.class);
        assertThat(token.getAuthorities()).isEmpty();
    }

    @Test
    void convert_WithMissingClientResource_ShouldReturnTokenWithoutRoles() {
        // Arrange
        Map<String, Object> resourceAccess = new HashMap<>();
        resourceAccess.put("different-client", Map.of("roles", Arrays.asList("admin")));

        when(mockJwt.getClaim("resource_access")).thenReturn(resourceAccess);
        when(mockJwt.getClaim("preferred_username")).thenReturn("johndoe");
        when(mockJwt.getClaimAsString("sub")).thenReturn("user-123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("johndoe");
        when(mockJwt.getClaims()).thenReturn(Map.of("sub", "user-123"));
        when(mockJwt.getIssuedAt()).thenReturn(now);
        when(mockJwt.getExpiresAt()).thenReturn(futureExpiry);

        // Act
        AbstractAuthenticationToken token = jwtAuthConverter.convert(mockJwt);

        // Assert
        assertThat(token).isInstanceOf(JwtAuthenticationToken.class);
        assertThat(token.getAuthorities()).isEmpty();
    }

    @Test
    void convert_WithNullRoles_ShouldReturnTokenWithoutRoles() {
        // Arrange
        Map<String, Object> resourceAccess = new HashMap<>();
        Map<String, Object> clientResource = new HashMap<>();
        clientResource.put("roles", null);
        resourceAccess.put("shop-manager", clientResource);

        when(mockJwt.getClaim("resource_access")).thenReturn(resourceAccess);
        when(mockJwt.getClaim("preferred_username")).thenReturn("johndoe");
        when(mockJwt.getClaimAsString("sub")).thenReturn("user-123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("johndoe");
        when(mockJwt.getClaims()).thenReturn(Map.of("sub", "user-123"));
        when(mockJwt.getIssuedAt()).thenReturn(now);
        when(mockJwt.getExpiresAt()).thenReturn(futureExpiry);

        // Act
        AbstractAuthenticationToken token = jwtAuthConverter.convert(mockJwt);

        // Assert
        assertThat(token).isInstanceOf(JwtAuthenticationToken.class);
        assertThat(token.getAuthorities()).isEmpty();
    }

    @Test
    void convert_WithEmptyRoles_ShouldReturnTokenWithoutRoles() {
        // Arrange
        Map<String, Object> resourceAccess = new HashMap<>();
        Map<String, Object> clientResource = new HashMap<>();
        clientResource.put("roles", Collections.emptyList());
        resourceAccess.put("shop-manager", clientResource);

        when(mockJwt.getClaim("resource_access")).thenReturn(resourceAccess);
        when(mockJwt.getClaim("preferred_username")).thenReturn("johndoe");
        when(mockJwt.getClaimAsString("sub")).thenReturn("user-123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("johndoe");
        when(mockJwt.getClaims()).thenReturn(Map.of("sub", "user-123"));
        when(mockJwt.getIssuedAt()).thenReturn(now);
        when(mockJwt.getExpiresAt()).thenReturn(futureExpiry);

        // Act
        AbstractAuthenticationToken token = jwtAuthConverter.convert(mockJwt);

        // Assert
        assertThat(token).isInstanceOf(JwtAuthenticationToken.class);
        assertThat(token.getAuthorities()).isEmpty();
    }

    @Test
    void convert_WithSingleRole_ShouldReturnTokenWithCorrectAuthority() {
        // Arrange
        Map<String, Object> resourceAccess = new HashMap<>();
        Map<String, Object> clientResource = new HashMap<>();
        List<String> roles = Arrays.asList("shop_manager");
        clientResource.put("roles", roles);
        resourceAccess.put("shop-manager", clientResource);

        when(mockJwt.getClaim("resource_access")).thenReturn(resourceAccess);
        when(mockJwt.getClaim("preferred_username")).thenReturn("johndoe");
        when(mockJwt.getClaimAsString("sub")).thenReturn("user-123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("johndoe");
        when(mockJwt.getClaims()).thenReturn(Map.of("sub", "user-123"));
        when(mockJwt.getIssuedAt()).thenReturn(now);
        when(mockJwt.getExpiresAt()).thenReturn(futureExpiry);

        // Act
        AbstractAuthenticationToken token = jwtAuthConverter.convert(mockJwt);

        // Assert
        assertThat(token).isInstanceOf(JwtAuthenticationToken.class);
        Collection<GrantedAuthority> authorities = token.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities)
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_SHOP_MANAGER");
    }

    @Test
    void convert_WithMixedCaseRoles_ShouldReturnUppercaseAuthorities() {
        // Arrange
        Map<String, Object> resourceAccess = new HashMap<>();
        Map<String, Object> clientResource = new HashMap<>();
        List<String> roles = Arrays.asList("Admin", "User", "MANAGER", "owner");
        clientResource.put("roles", roles);
        resourceAccess.put("shop-manager", clientResource);

        when(mockJwt.getClaim("resource_access")).thenReturn(resourceAccess);
        when(mockJwt.getClaim("preferred_username")).thenReturn("johndoe");
        when(mockJwt.getClaimAsString("sub")).thenReturn("user-123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("johndoe");
        when(mockJwt.getClaims()).thenReturn(Map.of("sub", "user-123"));
        when(mockJwt.getIssuedAt()).thenReturn(now);
        when(mockJwt.getExpiresAt()).thenReturn(futureExpiry);

        // Act
        AbstractAuthenticationToken token = jwtAuthConverter.convert(mockJwt);

        // Assert
        assertThat(token).isInstanceOf(JwtAuthenticationToken.class);
        Collection<GrantedAuthority> authorities = token.getAuthorities();
        assertThat(authorities).hasSize(4);
        assertThat(authorities)
            .extracting(GrantedAuthority::getAuthority)
            .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER", "ROLE_MANAGER", "ROLE_OWNER");
    }

    @Test
    void convert_WithNullPreferredUsername_ShouldReturnNullName() {
        // Arrange
        Map<String, Object> resourceAccess = new HashMap<>();
        Map<String, Object> clientResource = new HashMap<>();
        clientResource.put("roles", Arrays.asList("user"));
        resourceAccess.put("shop-manager", clientResource);

        when(mockJwt.getClaim("resource_access")).thenReturn(resourceAccess);
        when(mockJwt.getClaim("preferred_username")).thenReturn(null);
        when(mockJwt.getClaimAsString("sub")).thenReturn("user-123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn(null);
        when(mockJwt.getClaims()).thenReturn(Map.of("sub", "user-123"));
        when(mockJwt.getIssuedAt()).thenReturn(now);
        when(mockJwt.getExpiresAt()).thenReturn(futureExpiry);

        // Act
        AbstractAuthenticationToken token = jwtAuthConverter.convert(mockJwt);

        // Assert
        assertThat(token).isInstanceOf(JwtAuthenticationToken.class);
        assertThat(token.getName()).isNull();
        assertThat(token.getAuthorities()).hasSize(1);
    }

    @Test
    void convert_ShouldSetRolesOnPrincipal() {
        // Arrange
        Map<String, Object> resourceAccess = new HashMap<>();
        Map<String, Object> clientResource = new HashMap<>();
        List<String> roles = Arrays.asList("admin", "manager");
        clientResource.put("roles", roles);
        resourceAccess.put("shop-manager", clientResource);

        when(mockJwt.getClaim("resource_access")).thenReturn(resourceAccess);
        when(mockJwt.getClaim("preferred_username")).thenReturn("johndoe");
        when(mockJwt.getClaimAsString("sub")).thenReturn("user-123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("johndoe");
        when(mockJwt.getClaimAsString("tenant_id")).thenReturn("tenant-456");
        when(mockJwt.getClaimAsString("email")).thenReturn("john.doe@example.com");
        when(mockJwt.getClaims()).thenReturn(Map.of("sub", "user-123"));
        when(mockJwt.getIssuedAt()).thenReturn(now);
        when(mockJwt.getExpiresAt()).thenReturn(futureExpiry);

        // Act
        AbstractAuthenticationToken token = jwtAuthConverter.convert(mockJwt);

        // Assert - This test verifies that the converter creates a JwtPrincipal with roles set
        assertThat(token).isInstanceOf(JwtAuthenticationToken.class);
        // Note: The roles are set on the JwtPrincipal object, but it's not directly accessible through the token
        // This test mainly verifies that the conversion completes without errors when roles are present
        assertThat(token.getAuthorities())
            .extracting(GrantedAuthority::getAuthority)
            .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_MANAGER");
    }
}