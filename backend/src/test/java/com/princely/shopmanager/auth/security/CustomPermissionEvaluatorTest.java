package com.princely.shopmanager.auth.security;

import com.princely.shopmanager.core.domain.Permission;
import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomPermissionEvaluator.
 *
 * Tests permission evaluation logic including:
 * - Permission lookup from database via user roles
 * - Handling of missing users
 * - Handling of users without roles
 * - Handling of null/invalid inputs
 * - Different authentication types
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomPermissionEvaluator Unit Tests")
class CustomPermissionEvaluatorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    private CustomPermissionEvaluator permissionEvaluator;

    @BeforeEach
    void setUp() {
        permissionEvaluator = new CustomPermissionEvaluator(userRepository);
    }

    @Test
    @DisplayName("Should return true when user has required permission")
    void shouldReturnTrueWhenUserHasPermission() {
        // Given
        String username = "manager@shopmanager.com";
        String requiredPermission = "SHOP_CREATE";

        JwtPrincipal principal = createTestPrincipal(username);
        when(authentication.getPrincipal()).thenReturn(principal);

        User user = createUserWithPermissions(username, Set.of("SHOP_CREATE", "SHOP_READ"));
        when(userRepository.findByEmailWithPermissions(username)).thenReturn(Optional.of(user));

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication,
            null,
            requiredPermission
        );

        // Then
        assertThat(hasPermission).isTrue();
        verify(userRepository).findByEmailWithPermissions(username);
    }

    @Test
    @DisplayName("Should return false when user does not have required permission")
    void shouldReturnFalseWhenUserDoesNotHavePermission() {
        // Given
        String username = "employee@shopmanager.com";
        String requiredPermission = "SHOP_DELETE";

        JwtPrincipal principal = createTestPrincipal(username);
        when(authentication.getPrincipal()).thenReturn(principal);

        User user = createUserWithPermissions(username, Set.of("PRODUCT_READ", "SALES_CREATE"));
        when(userRepository.findByEmailWithPermissions(username)).thenReturn(Optional.of(user));

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication,
            null,
            requiredPermission
        );

        // Then
        assertThat(hasPermission).isFalse();
        verify(userRepository).findByEmailWithPermissions(username);
    }

    @Test
    @DisplayName("Should return false when user is not found in database")
    void shouldReturnFalseWhenUserNotFound() {
        // Given
        String username = "nonexistent@example.com";
        String requiredPermission = "SHOP_CREATE";

        JwtPrincipal principal = createTestPrincipal(username);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findByEmailWithPermissions(username)).thenReturn(Optional.empty());

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication,
            null,
            requiredPermission
        );

        // Then
        assertThat(hasPermission).isFalse();
        verify(userRepository).findByEmailWithPermissions(username);
    }

    @Test
    @DisplayName("Should return false when user has no roles")
    void shouldReturnFalseWhenUserHasNoRoles() {
        // Given
        String username = "norole@shopmanager.com";
        String requiredPermission = "SHOP_CREATE";

        JwtPrincipal principal = createTestPrincipal(username);
        when(authentication.getPrincipal()).thenReturn(principal);

        User user = createUserWithPermissions(username, Set.of());
        when(userRepository.findByEmailWithPermissions(username)).thenReturn(Optional.of(user));

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication,
            null,
            requiredPermission
        );

        // Then
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("Should return false when authentication is null")
    void shouldReturnFalseWhenAuthenticationIsNull() {
        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            null,
            null,
            "SHOP_CREATE"
        );

        // Then
        assertThat(hasPermission).isFalse();
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should return false when target permission is null")
    void shouldReturnFalseWhenTargetPermissionIsNull() {
        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication,
            null,
            null
        );

        // Then
        assertThat(hasPermission).isFalse();
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should return false when target permission is empty string")
    void shouldReturnFalseWhenTargetPermissionIsEmpty() {
        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication,
            null,
            ""
        );

        // Then
        assertThat(hasPermission).isFalse();
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should handle CustomJwtAuthenticationToken correctly")
    void shouldHandleCustomJwtAuthenticationToken() {
        // Given
        String username = "manager@shopmanager.com";
        String requiredPermission = "PRODUCT_CREATE";

        JwtPrincipal principal = createTestPrincipal(username);
        Jwt jwt = mock(Jwt.class);
        CustomJwtAuthenticationToken token = new CustomJwtAuthenticationToken(
            jwt,
            null,
            principal
        );

        User user = createUserWithPermissions(username, Set.of("PRODUCT_CREATE"));
        when(userRepository.findByEmailWithPermissions(username)).thenReturn(Optional.of(user));

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            token,
            null,
            requiredPermission
        );

        // Then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("Should check multiple permissions correctly")
    void shouldCheckMultiplePermissionsCorrectly() {
        // Given
        String username = "manager@shopmanager.com";
        JwtPrincipal principal = createTestPrincipal(username);
        when(authentication.getPrincipal()).thenReturn(principal);

        User user = createUserWithPermissions(
            username,
            Set.of("SHOP_CREATE", "SHOP_READ", "SHOP_UPDATE", "PRODUCT_READ")
        );
        when(userRepository.findByEmailWithPermissions(username)).thenReturn(Optional.of(user));

        // When/Then
        assertThat(permissionEvaluator.hasPermission(authentication, null, "SHOP_CREATE")).isTrue();
        assertThat(permissionEvaluator.hasPermission(authentication, null, "SHOP_READ")).isTrue();
        assertThat(permissionEvaluator.hasPermission(authentication, null, "SHOP_UPDATE")).isTrue();
        assertThat(permissionEvaluator.hasPermission(authentication, null, "PRODUCT_READ")).isTrue();
        assertThat(permissionEvaluator.hasPermission(authentication, null, "SHOP_DELETE")).isFalse();
    }

    @Test
    @DisplayName("Should return false for hasPermission with Serializable targetId (not implemented)")
    void shouldReturnFalseForSerializableTargetId() {
        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication,
            "target-id",
            "SomeType",
            "SOME_PERMISSION"
        );

        // Then
        assertThat(hasPermission).isFalse();
    }

    // ==========================================
    // Helper Methods
    // ==========================================

    private JwtPrincipal createTestPrincipal(String email) {
        JwtPrincipal principal = new JwtPrincipal();
        principal.setEmail(email);
        principal.setPreferredUsername(email);
        principal.setTenantId("test-tenant-id");
        principal.setShopId("test-shop-id");
        return principal;
    }

    private User createUserWithPermissions(String email, Set<String> permissionNames) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(email);

        Role role = new Role();
        role.setName("TEST_ROLE");

        Set<Permission> permissions = Set.of();
        if (!permissionNames.isEmpty()) {
            permissions = permissionNames.stream()
                .map(name -> {
                    Permission permission = new Permission();
                    permission.setName(name);
                    permission.setResource(name.split("_")[0]);
                    permission.setAction(name.split("_")[1]);
                    return permission;
                })
                .collect(java.util.stream.Collectors.toSet());
        }

        role.setPermissions(permissions);
        user.setRoles(Set.of(role));

        return user;
    }
}
