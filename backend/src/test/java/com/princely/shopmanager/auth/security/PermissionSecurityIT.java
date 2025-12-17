package com.princely.shopmanager.auth.security;

import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import com.princely.shopmanager.core.domain.Permission;
import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.dto.ShopCreateRequest;
import com.princely.shopmanager.core.dto.ShopResponse;
import com.princely.shopmanager.core.repository.PermissionRepository;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for permission-based security.
 *
 * Tests the complete permission evaluation flow:
 * - JWT authentication
 * - Database permission lookup
 * - Method-level security with @PreAuthorize
 * - Authorization decisions based on user permissions
 * - Multi-tenant isolation with permissions
 *
 * Uses TestContainers for full integration testing.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Permission-Based Security Integration Tests")
@Disabled("Temporarily disabled during IT reduction - will be replaced with minimal happy path test and unit tests")
class PermissionSecurityIT extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "security-test-tenant";
        setTenantContext(tenantId);
    }

    @Test
    @DisplayName("Should allow access when user has required permission - SHOP_CREATE")
    void shouldAllowAccessWhenUserHasShopCreatePermission() {
        // Given
        String userEmail = "owner@shopmanager.com";
        setupUserWithPermissions(userEmail, "OWNER", Set.of("SHOP_CREATE", "SHOP_READ"));

        ShopCreateRequest request = createSampleShopCreateRequest("Test Shop");

        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedPost(
            "/shops",
            request,
            userEmail,
            ShopResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Test Shop");
    }

    @Test
    @DisplayName("Should deny access when user lacks required permission - SHOP_CREATE")
    void shouldDenyAccessWhenUserLacksShopCreatePermission() {
        // Given
        String userEmail = "employee@shopmanager.com";
        setupUserWithPermissions(userEmail, "EMPLOYEE", Set.of("PRODUCT_READ", "SALES_CREATE"));

        ShopCreateRequest request = createSampleShopCreateRequest("Test Shop");

        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedPost(
            "/shops",
            request,
            userEmail,
            ShopResponse.class,
            "EMPLOYEE"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Should allow MANAGER role to list shops with SHOP_LIST permission")
    void shouldAllowManagerToListShopsWithPermission() {
        // Given
        String userEmail = "manager@shopmanager.com";
        setupUserWithPermissions(
            userEmail,
            "MANAGER",
            Set.of("SHOP_LIST", "SHOP_READ", "PRODUCT_CREATE", "SALES_CREATE")
        );

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/shops",
            userEmail,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Should deny EMPLOYEE role access to shops without SHOP_LIST permission")
    void shouldDenyEmployeeAccessToListShopsWithoutPermission() {
        // Given
        String userEmail = "cashier@shopmanager.com";
        setupUserWithPermissions(
            userEmail,
            "EMPLOYEE",
            Set.of("SALES_CREATE", "PRODUCT_READ")
        );

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/shops",
            userEmail,
            String.class,
            "EMPLOYEE"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Should allow access to multiple endpoints with correct permissions")
    void shouldAllowAccessToMultipleEndpointsWithPermissions() {
        // Given
        String userEmail = "manager@shopmanager.com";
        setupUserWithPermissions(
            userEmail,
            "MANAGER",
            Set.of(
                "SHOP_LIST", "SHOP_READ", "SHOP_CREATE",
                "PRODUCT_LIST", "PRODUCT_READ", "PRODUCT_CREATE",
                "SALES_CREATE", "SALES_LIST"
            )
        );

        // When - Test multiple endpoints
        ResponseEntity<String> shopsResponse = performAuthenticatedGet(
            "/shops",
            userEmail,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(shopsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Should handle user with no roles gracefully")
    void shouldHandleUserWithNoRolesGracefully() {
        // Given
        String userEmail = "norole@shopmanager.com";
        setupUserWithPermissions(userEmail, null, Set.of());

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/shops",
            userEmail,
            String.class,
            "USER"  // Generic role
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Should validate permissions independently of roles")
    void shouldValidatePermissionsIndependentlyOfRoles() {
        // Given - Create a custom role with specific permissions
        String userEmail = "customrole@shopmanager.com";
        setupUserWithPermissions(
            userEmail,
            "CUSTOM_ROLE",
            Set.of("SHOP_LIST", "SHOP_READ")  // Only read permissions, no write
        );

        // When - Try to create (should fail)
        ShopCreateRequest createRequest = createSampleShopCreateRequest("Test Shop");
        ResponseEntity<ShopResponse> createResponse = performAuthenticatedPost(
            "/shops",
            createRequest,
            userEmail,
            ShopResponse.class,
            "CUSTOM_ROLE"
        );

        // Then
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // When - Try to list (should succeed)
        ResponseEntity<String> listResponse = performAuthenticatedGet(
            "/shops",
            userEmail,
            String.class,
            "CUSTOM_ROLE"
        );

        // Then
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Should enforce multi-tenant isolation with permissions")
    void shouldEnforceMultiTenantIsolationWithPermissions() {
        // Given
        String tenant1Id = "tenant-1";
        String tenant2Id = "tenant-2";

        String user1Email = "user1@tenant1.com";
        String user2Email = "user2@tenant2.com";

        // Setup users in different tenants with same permissions
        setTenantContext(tenant1Id);
        setupUserWithPermissions(user1Email, "MANAGER", Set.of("SHOP_LIST", "SHOP_READ"));

        setTenantContext(tenant2Id);
        setupUserWithPermissions(user2Email, "MANAGER", Set.of("SHOP_LIST", "SHOP_READ"));

        // When - User 1 accesses their shops
        setTenantContext(tenant1Id);
        ResponseEntity<String> user1Response = performAuthenticatedGet(
            "/shops",
            user1Email,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(user1Response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // When - User 2 accesses their shops
        setTenantContext(tenant2Id);
        ResponseEntity<String> user2Response = performAuthenticatedGet(
            "/shops",
            user2Email,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(user2Response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ==========================================
    // Helper Methods
    // ==========================================

    /**
     * Setup a user with specific role and permissions in the database.
     */
    private void setupUserWithPermissions(String email, String roleName, Set<String> permissionNames) {
        // Create or get role
        Role role;
        if (roleName != null) {
            role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    newRole.setDescription("Test role: " + roleName);
                    return roleRepository.save(newRole);
                });

            // Add permissions to role
            Set<Permission> permissions = permissionNames.stream()
                .map(permissionName -> permissionRepository.findByName(permissionName)
                    .orElseGet(() -> {
                        Permission newPermission = new Permission();
                        newPermission.setName(permissionName);
                        newPermission.setResource(permissionName.split("_")[0]);
                        newPermission.setAction(permissionName.split("_")[1]);
                        return permissionRepository.save(newPermission);
                    }))
                .collect(java.util.stream.Collectors.toSet());

            role.setPermissions(permissions);
            role = roleRepository.save(role);
        } else {
            role = null;
        }

        // Create or update user
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setUsername(email);
                newUser.setFirstName("Test");
                newUser.setLastName("User");
                newUser.setStatus(User.UserStatus.ACTIVE);
                return newUser;
            });

        if (role != null) {
            user.setRoles(Set.of(role));
        } else {
            user.setRoles(Set.of());
        }

        userRepository.save(user);
    }
}
