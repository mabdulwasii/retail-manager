package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.domain.Permission;
import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.PermissionRepository;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.test.TestConstants;
import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import com.princely.shopmanager.test.security.WithMockPermissions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("UserController Integration Tests")
class UserControllerIT extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private User testUser;
    private Tenant testTenant;
    private Shop testShop;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        shopRepository.deleteAll();
        tenantRepository.deleteAll();

        // Create test tenant
        testTenant = Tenant.builder()
            .name("Test Company")
            .contactEmail("admin@testcompany.com")
            .primaryAddress("123 Test St")
            .status(Tenant.TenantStatus.ACTIVE)
            .build();
        testTenant = tenantRepository.saveAndFlush(testTenant);

        // Create test shop
        testShop = Shop.builder()
            .name("Test Shop")
            .address("456 Shop St")
            .email("shop@test.com")
            .phoneNumber("+1234567890")
            .tenant(testTenant)
            .status(Shop.ShopStatus.ACTIVE)
            .build();
        testShop = shopRepository.saveAndFlush(testShop);

        testUser = User.builder()
            .keycloakId("keycloak-456")
            .username("john.doe")
            .email("john.doe@example.com")
            .firstName("John")
            .lastName("Doe")
            .phoneNumber("+1234567890")
            .tenant(testTenant)
            .shop(testShop)
            .status(User.UserStatus.ACTIVE)
            .build();
    }

    @Test
    @DisplayName("Should return user profile when user exists in database")
    @WithMockPermissions(role = "MANAGER", username = "john.doe", tenantId = "tenant-123", shopId = "shop-456")
    void shouldReturnUserProfileWhenUserExists() throws Exception {
        // Given - Save user to database with keycloak ID matching mock user
        testUser.setKeycloakId("750e8400-e29b-41d4-a716-446655440000"); // Match TestConstants.MOCK_USER_ID
        User savedUser = userRepository.saveAndFlush(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(savedUser.getId()))
            .andExpect(jsonPath("$.username").value("john.doe"))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.fullName").value("John Doe"))
            .andExpect(jsonPath("$.phoneNumber").value("+1234567890"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            // IMPORTANT: tenantId and shopId come from DATABASE, not JWT (to prevent staleness)
            .andExpect(jsonPath("$.tenantId").value(testTenant.getId()))
            .andExpect(jsonPath("$.shopId").value(testShop.getId()))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("Should return 500 when user not found in database")
    @WithMockPermissions(role = "INVESTOR", username = "jane.doe", tenantId = "tenant-789", shopId = "shop-999")
    void shouldReturnJwtProfileWhenUserNotInDatabase() throws Exception {
        // Given - No user in database (repository is cleared in @BeforeEach)
        // Controller requires user to exist in database (synced via UserSyncService)

        // When & Then - Expect internal server error since user not synced
        mockMvc.perform(get("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should return 401 when user is not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should allow access to any authenticated user regardless of role")
    @WithMockPermissions(username = "john.doe", tenantId = "tenant-123")
    void shouldAllowAnyAuthenticatedUser() throws Exception {
        // Given - Create user in database
        testUser.setKeycloakId("750e8400-e29b-41d4-a716-446655440000");
        testUser.setUsername("john.doe");
        userRepository.saveAndFlush(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("john.doe"));
    }

    @Test
    @DisplayName("Should accept OWNER role")
    @WithMockPermissions(role = "OWNER", username = "admin.user")
    void shouldAcceptTenantAdminRole() throws Exception {
        // Given - Save user to database
        testUser.setKeycloakId("750e8400-e29b-41d4-a716-446655440000");
        User savedUser = userRepository.saveAndFlush(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedUser.getId()));
    }

    @Test
    @DisplayName("Should accept INVESTOR role")
    @WithMockPermissions(role = "INVESTOR", username = "investor.user")
    void shouldAcceptInvestorRole() throws Exception {
        // Given - Create and save investor user
        User investorUser = User.builder()
            .keycloakId("750e8400-e29b-41d4-a716-446655440000")
            .username("investor.user")
            .email("investor@example.com")
            .firstName("Investor")
            .lastName("User")
            .phoneNumber("+1987654321")
            .tenant(testTenant)
            .status(User.UserStatus.ACTIVE)
            .build();

        User savedInvestor = userRepository.saveAndFlush(investorUser);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedInvestor.getId()));
    }

    @Test
    @DisplayName("Should accept EMPLOYEE role")
    @WithMockPermissions(role = "EMPLOYEE", username = "employee.user")
    void shouldAcceptShopEmployeeRole() throws Exception {
        // Given - Save user to database
        testUser.setKeycloakId("750e8400-e29b-41d4-a716-446655440000");
        User savedUser = userRepository.saveAndFlush(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedUser.getId()));
    }

    @Test
    @DisplayName("Should return 500 when user not synced to database")
    @WithMockPermissions(role = "MANAGER", username = "minimal.user")
    void shouldHandleMissingJwtClaims() throws Exception {
        // Given - No user in database for this Keycloak ID
        // Controller requires user to exist in database (synced via UserSyncService)

        // When & Then - Expect internal server error since user not synced
        mockMvc.perform(get("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
    }

    // ========== Tests for GET /api/users (System-wide user listing) ==========

    @Test
    @DisplayName("GET /api/users - System Admin can list all users")
    @WithMockPermissions(role = "SYSTEM_ADMIN", username = "admin.user")
    void getAllUsers_SystemAdmin_Success() throws Exception {
        // Given - Create system admin user with proper permissions
        createSystemAdminUser();

        // Given - Create multiple users
        User user1 = userRepository.saveAndFlush(testUser);
        User user2 = User.builder()
            .keycloakId("kc-2")
            .username("jane.smith")
            .email("jane@example.com")
            .firstName("Jane")
            .lastName("Smith")
            .phoneNumber("+9876543210")
            .tenant(testTenant)
            .shop(testShop)
            .status(User.UserStatus.ACTIVE)
            .build();
        userRepository.saveAndFlush(user2);

        // When & Then
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))  // admin.user + testUser + jane.smith
            .andExpect(jsonPath("$[0].username").exists())
            .andExpect(jsonPath("$[1].username").exists())
            .andExpect(jsonPath("$[2].username").exists());
    }

    @Test
    @DisplayName("GET /api/users - System Admin can filter by status")
    @WithMockPermissions(role = "SYSTEM_ADMIN", username = "admin.user")
    void getAllUsers_FilterByStatus_Success() throws Exception {
        // Given - Create system admin user with proper permissions
        createSystemAdminUser();

        // Given - Create users with different statuses
        testUser.setStatus(User.UserStatus.ACTIVE);
        userRepository.saveAndFlush(testUser);

        User inactiveUser = User.builder()
            .keycloakId("kc-inactive")
            .username("inactive.user")
            .email("inactive@example.com")
            .firstName("Inactive")
            .lastName("User")
            .phoneNumber("+1111111111")
            .tenant(testTenant)
            .shop(testShop)
            .status(User.UserStatus.INACTIVE)
            .build();
        userRepository.saveAndFlush(inactiveUser);

        // When & Then - Filter for ACTIVE only (admin.user + testUser)
        mockMvc.perform(get("/api/users")
                .param("status", "ACTIVE")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))  // admin.user + john.doe (both ACTIVE)
            .andExpect(jsonPath("$[*].status").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("ACTIVE"))));
    }

    @Test
    @DisplayName("GET /api/users - Non-System Admin gets 403")
    @WithMockPermissions(role = "MANAGER", username = "manager.user")
    void getAllUsers_NonSystemAdmin_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/users - Unauthenticated user gets 401")
    void getAllUsers_Unauthenticated_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    /**
     * Creates a system admin user with USER_LIST_ALL permission.
     * Required for hasPermission() checks that query the database.
     */
    private void createSystemAdminUser() {
        // Find or create USER_LIST_ALL permission
        Permission userListAllPermission = permissionRepository.findByName("USER_LIST_ALL")
            .orElseGet(() -> {
                Permission perm = Permission.builder()
                    .name("USER_LIST_ALL")
                    .description("List all users across all tenants in the system")
                    .resource("USER")
                    .action("LIST_ALL")
                    .build();
                return permissionRepository.save(perm);
            });

        // Find or create SYSTEM_ADMIN role with USER_LIST_ALL permission
        Role systemAdminRole = roleRepository.findByName("SYSTEM_ADMIN")
            .orElseGet(() -> {
                Role role = Role.builder()
                    .name("SYSTEM_ADMIN")
                    .description("System-level administrative access")
                    .isSystem(true)
                    .build();
                return roleRepository.save(role);
            });

        // Add permission to role if not already present
        if (!systemAdminRole.getPermissions().contains(userListAllPermission)) {
            systemAdminRole.getPermissions().add(userListAllPermission);
            roleRepository.save(systemAdminRole);
        }

        // Create system admin user with TestConstants.ADMIN_EMAIL
        User adminUser = User.builder()
            .keycloakId(TestConstants.KC_ADMIN_001)
            .username("admin.user")
            .email(TestConstants.ADMIN_EMAIL)
            .firstName("Admin")
            .lastName("User")
            .phoneNumber("+1234567890")
            .tenant(testTenant)
            .shop(testShop)
            .status(User.UserStatus.ACTIVE)
            .build();

        adminUser.getRoles().add(systemAdminRole);
        userRepository.save(adminUser);
    }
}