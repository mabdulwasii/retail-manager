package com.princely.shopmanager.core.service;

import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.dto.UserCreateRequest;
import com.princely.shopmanager.core.dto.UserUpdateRequest;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.shared.service.AuditService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 * Tests user management business logic without Spring context or Keycloak integration.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserService userService;

    private static final String TEST_TENANT_ID = "test-tenant";
    private static final String TEST_USER_ID = "test-user";
    private static final String TEST_SHOP_ID = "test-shop";

    // ============== getUserById Tests ==============

    @Test
    @DisplayName("getUserById should return user when found")
    void getUserByIdShouldReturnUser() {
        // Given
        User expectedUser = createUser(TEST_USER_ID, "test@example.com", "testuser");
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(expectedUser));

        // When
        User result = userService.getUserById(TEST_USER_ID);

        // Then
        assertThat(result).isEqualTo(expectedUser);
    }

    @Test
    @DisplayName("getUserById should return null when not found")
    void getUserByIdShouldReturnNullWhenNotFound() {
        // Given
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When
        User result = userService.getUserById("nonexistent");

        // Then
        assertThat(result).isNull();
    }

    // ============== getUserByEmail Tests ==============

    @Test
    @DisplayName("getUserByEmail should return user when found")
    void getUserByEmailShouldReturnUser() {
        // Given
        User expectedUser = createUser(TEST_USER_ID, "test@example.com", "testuser");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(expectedUser));

        // When
        User result = userService.getUserByEmail("test@example.com");

        // Then
        assertThat(result).isEqualTo(expectedUser);
    }

    @Test
    @DisplayName("getUserByEmail should return null when not found")
    void getUserByEmailShouldReturnNullWhenNotFound() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        User result = userService.getUserByEmail("nonexistent@example.com");

        // Then
        assertThat(result).isNull();
    }

    // ============== getUserByKeycloakId Tests ==============

    @Test
    @DisplayName("getUserByKeycloakId should return user when found")
    void getUserByKeycloakIdShouldReturnUser() {
        // Given
        User expectedUser = createUser(TEST_USER_ID, "test@example.com", "testuser");
        when(userRepository.findByKeycloakId("keycloak-123")).thenReturn(Optional.of(expectedUser));

        // When
        User result = userService.getUserByKeycloakId("keycloak-123");

        // Then
        assertThat(result).isEqualTo(expectedUser);
    }

    @Test
    @DisplayName("getUserByKeycloakId should return null when not found")
    void getUserByKeycloakIdShouldReturnNullWhenNotFound() {
        // Given
        when(userRepository.findByKeycloakId("nonexistent")).thenReturn(Optional.empty());

        // When
        User result = userService.getUserByKeycloakId("nonexistent");

        // Then
        assertThat(result).isNull();
    }

    // ============== createUser Tests ==============

    @Test
    @DisplayName("createUser should create user successfully with valid data")
    void createUserShouldCreateUserSuccessfully() {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
            .username("newuser")
            .email("newuser@example.com")
            .firstName("New")
            .lastName("User")
            .phoneNumber("+1234567890")
            .password("password123")
            .shopId(TEST_SHOP_ID)
            .roles(Set.of("MANAGER"))
            .build();

        Tenant tenant = createTenant(TEST_TENANT_ID);
        Role managerRole = createRole("MANAGER");
        Shop shop = createShop(TEST_SHOP_ID, tenant);
        User savedUser = createUser("new-user-id", "newuser@example.com", "newuser");

        when(tenantRepository.findById(TEST_TENANT_ID)).thenReturn(Optional.of(tenant));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(roleRepository.findById("MANAGER")).thenReturn(Optional.empty());
        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(managerRole));
        when(shopRepository.findById(TEST_SHOP_ID)).thenReturn(Optional.of(shop));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.createUser(TEST_TENANT_ID, request);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser should throw exception when tenant not found")
    void createUserShouldThrowWhenTenantNotFound() {
        // Given
        UserCreateRequest request = createValidUserRequest();
        when(tenantRepository.findById(TEST_TENANT_ID)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.createUser(TEST_TENANT_ID, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Tenant not found");
    }

    @Test
    @DisplayName("createUser should throw exception when username already exists")
    void createUserShouldThrowWhenUsernameExists() {
        // Given
        UserCreateRequest request = createValidUserRequest();
        Tenant tenant = createTenant(TEST_TENANT_ID);

        when(tenantRepository.findById(TEST_TENANT_ID)).thenReturn(Optional.of(tenant));
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.createUser(TEST_TENANT_ID, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Username already exists");
    }

    @Test
    @DisplayName("createUser should throw exception when email already exists")
    void createUserShouldThrowWhenEmailExists() {
        // Given
        UserCreateRequest request = createValidUserRequest();
        Tenant tenant = createTenant(TEST_TENANT_ID);

        when(tenantRepository.findById(TEST_TENANT_ID)).thenReturn(Optional.of(tenant));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.createUser(TEST_TENANT_ID, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email already exists");
    }

    @Test
    @DisplayName("createUser should throw exception when no roles specified")
    void createUserShouldThrowWhenNoRolesSpecified() {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
            .username("newuser")
            .email("newuser@example.com")
            .firstName("New")
            .lastName("User")
            .shopId(TEST_SHOP_ID)
            .roles(null) // No roles
            .build();

        Tenant tenant = createTenant(TEST_TENANT_ID);

        when(tenantRepository.findById(TEST_TENANT_ID)).thenReturn(Optional.of(tenant));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> userService.createUser(TEST_TENANT_ID, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("At least one role must be specified");
    }

    @Test
    @DisplayName("createUser should throw exception when shopId not provided")
    void createUserShouldThrowWhenShopIdNotProvided() {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
            .username("newuser")
            .email("newuser@example.com")
            .firstName("New")
            .lastName("User")
            .shopId(null) // No shop ID
            .roles(Set.of("MANAGER"))
            .build();

        Tenant tenant = createTenant(TEST_TENANT_ID);
        Role managerRole = createRole("MANAGER");

        when(tenantRepository.findById(TEST_TENANT_ID)).thenReturn(Optional.of(tenant));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(roleRepository.findById("MANAGER")).thenReturn(Optional.empty());
        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(managerRole));

        // When/Then
        assertThatThrownBy(() -> userService.createUser(TEST_TENANT_ID, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Shop ID is required");
    }

    @Test
    @DisplayName("createUser should throw exception when shop not found")
    void createUserShouldThrowWhenShopNotFound() {
        // Given
        UserCreateRequest request = createValidUserRequest();
        Tenant tenant = createTenant(TEST_TENANT_ID);
        Role managerRole = createRole("MANAGER");

        when(tenantRepository.findById(TEST_TENANT_ID)).thenReturn(Optional.of(tenant));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(roleRepository.findById("MANAGER")).thenReturn(Optional.empty());
        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(managerRole));
        when(shopRepository.findById(TEST_SHOP_ID)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.createUser(TEST_TENANT_ID, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Shop not found");
    }

    @Test
    @DisplayName("createUser should throw exception when shop belongs to different tenant")
    void createUserShouldThrowWhenShopBelongsToDifferentTenant() {
        // Given
        UserCreateRequest request = createValidUserRequest();
        Tenant correctTenant = createTenant(TEST_TENANT_ID);
        Tenant wrongTenant = createTenant("different-tenant");
        Role managerRole = createRole("MANAGER");
        Shop shop = createShop(TEST_SHOP_ID, wrongTenant); // Wrong tenant

        when(tenantRepository.findById(TEST_TENANT_ID)).thenReturn(Optional.of(correctTenant));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(roleRepository.findById("MANAGER")).thenReturn(Optional.empty());
        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(managerRole));
        when(shopRepository.findById(TEST_SHOP_ID)).thenReturn(Optional.of(shop));

        // When/Then
        assertThatThrownBy(() -> userService.createUser(TEST_TENANT_ID, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("does not belong to tenant");
    }

    @Test
    @DisplayName("createUser should resolve role by name when ID not found")
    void createUserShouldResolveRoleByName() {
        // Given
        UserCreateRequest request = createValidUserRequest();
        Tenant tenant = createTenant(TEST_TENANT_ID);
        Role managerRole = createRole("MANAGER");
        Shop shop = createShop(TEST_SHOP_ID, tenant);
        User savedUser = createUser("new-user-id", "newuser@example.com", "newuser");

        when(tenantRepository.findById(TEST_TENANT_ID)).thenReturn(Optional.of(tenant));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(roleRepository.findById("MANAGER")).thenReturn(Optional.empty()); // Not found by ID
        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(managerRole)); // Found by name
        when(shopRepository.findById(TEST_SHOP_ID)).thenReturn(Optional.of(shop));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.createUser(TEST_TENANT_ID, request);

        // Then
        assertThat(result).isNotNull();
        verify(roleRepository).findByName("MANAGER");
    }

    // ============== updateUser Tests ==============

    @Test
    @DisplayName("updateUser should update user successfully")
    void updateUserShouldUpdateUserSuccessfully() {
        // Given
        User existingUser = createUser(TEST_USER_ID, "old@example.com", "olduser");
        UserUpdateRequest request = UserUpdateRequest.builder()
            .email("new@example.com")
            .firstName("NewFirst")
            .lastName("NewLast")
            .phoneNumber("+9876543210")
            .status(User.UserStatus.ACTIVE)
            .build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        User result = userService.updateUser(TEST_USER_ID, request);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("updateUser should throw exception when user not found")
    void updateUserShouldThrowWhenUserNotFound() {
        // Given
        UserUpdateRequest request = UserUpdateRequest.builder().build();
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.updateUser("nonexistent", request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("updateUser should throw exception when email already exists")
    void updateUserShouldThrowWhenEmailExists() {
        // Given
        User existingUser = createUser(TEST_USER_ID, "old@example.com", "olduser");
        UserUpdateRequest request = UserUpdateRequest.builder()
            .email("taken@example.com")
            .build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.updateUser(TEST_USER_ID, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email already exists");
    }

    @Test
    @DisplayName("updateUser should allow keeping same email")
    void updateUserShouldAllowKeepingSameEmail() {
        // Given
        User existingUser = createUser(TEST_USER_ID, "same@example.com", "user");
        UserUpdateRequest request = UserUpdateRequest.builder()
            .email("same@example.com") // Same email
            .build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("same@example.com")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        User result = userService.updateUser(TEST_USER_ID, request);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).save(existingUser);
    }

    // ============== deleteUser Tests ==============

    @Test
    @DisplayName("deleteUser should soft delete user")
    void deleteUserShouldSoftDeleteUser() {
        // Given
        User user = createUser(TEST_USER_ID, "test@example.com", "testuser");
        user.setStatus(User.UserStatus.ACTIVE);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        userService.deleteUser(TEST_USER_ID);

        // Then
        assertThat(user.getStatus()).isEqualTo(User.UserStatus.INACTIVE);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("deleteUser should throw exception when user not found")
    void deleteUserShouldThrowWhenUserNotFound() {
        // Given
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.deleteUser("nonexistent"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found");
    }

    // Helper methods

    private User createUser(String id, String email, String username) {
        return User.builder()
            .id(id)
            .email(email)
            .username(username)
            .firstName("Test")
            .lastName("User")
            .status(User.UserStatus.ACTIVE)
            .tenant(createTenant(TEST_TENANT_ID))
            .roles(new HashSet<>())
            .build();
    }

    private Tenant createTenant(String id) {
        return Tenant.builder()
            .id(id)
            .name("Test Tenant")
            .build();
    }

    private Role createRole(String name) {
        return Role.builder()
            .id(UUID.randomUUID().toString())
            .name(name)
            .description("Test role: " + name)
            .isSystem(true)
            .build();
    }

    private Shop createShop(String id, Tenant tenant) {
        return Shop.builder()
            .id(id)
            .name("Test Shop")
            .tenant(tenant)
            .build();
    }

    private UserCreateRequest createValidUserRequest() {
        return UserCreateRequest.builder()
            .username("newuser")
            .email("newuser@example.com")
            .firstName("New")
            .lastName("User")
            .phoneNumber("+1234567890")
            .password("password123")
            .shopId(TEST_SHOP_ID)
            .roles(Set.of("MANAGER"))
            .build();
    }
}
