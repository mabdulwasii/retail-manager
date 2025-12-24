package com.princely.shopmanager.core.service;

import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.core.domain.Permission;
import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.dto.RoleCreateRequest;
import com.princely.shopmanager.core.dto.RoleUpdateRequest;
import com.princely.shopmanager.core.repository.PermissionRepository;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
 * Unit tests for RoleService.
 * Tests role management business logic without Spring context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService Unit Tests")
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private RoleService roleService;

    private static final String TEST_TENANT_ID = "test-tenant";
    private static final String TEST_USER_ID = "test-user";
    private static final String TEST_ROLE_ID = "test-role";
    private static final String TEST_PERMISSION_ID = "test-permission";

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(TEST_TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // ============== getAllRoles Tests ==============

    @Test
    @DisplayName("getAllRoles should return system and tenant roles")
    void getAllRolesShouldReturnSystemAndTenantRoles() {
        // Given
        List<Role> expectedRoles = List.of(
            createRole("OWNER", true, null),
            createRole("CUSTOM_ROLE", false, TEST_TENANT_ID)
        );
        when(roleRepository.findSystemAndTenantRoles(TEST_TENANT_ID)).thenReturn(expectedRoles);

        // When
        List<Role> result = roleService.getAllRoles();

        // Then
        assertThat(result).hasSize(2);
        verify(roleRepository).findSystemAndTenantRoles(TEST_TENANT_ID);
    }

    // ============== getRoleById Tests ==============

    @Test
    @DisplayName("getRoleById should return role when found")
    void getRoleByIdShouldReturnRoleWhenFound() {
        // Given
        Role expectedRole = createRole("MANAGER", true, null);
        when(roleRepository.findById(TEST_ROLE_ID)).thenReturn(Optional.of(expectedRole));

        // When
        Role result = roleService.getRoleById(TEST_ROLE_ID);

        // Then
        assertThat(result).isEqualTo(expectedRole);
        verify(roleRepository).findById(TEST_ROLE_ID);
    }

    @Test
    @DisplayName("getRoleById should throw exception when not found")
    void getRoleByIdShouldThrowWhenNotFound() {
        // Given
        when(roleRepository.findById(TEST_ROLE_ID)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> roleService.getRoleById(TEST_ROLE_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role not found");
    }

    // ============== getRoleByName Tests ==============

    @Test
    @DisplayName("getRoleByName should return role when found")
    void getRoleByNameShouldReturnRoleWhenFound() {
        // Given
        Role expectedRole = createRole("OWNER", true, null);
        when(roleRepository.findByName("OWNER")).thenReturn(Optional.of(expectedRole));

        // When
        Role result = roleService.getRoleByName("OWNER");

        // Then
        assertThat(result).isEqualTo(expectedRole);
        verify(roleRepository).findByName("OWNER");
    }

    @Test
    @DisplayName("getRoleByName should throw exception when not found")
    void getRoleByNameShouldThrowWhenNotFound() {
        // Given
        when(roleRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> roleService.getRoleByName("NONEXISTENT"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role not found");
    }

    // ============== getUserRoles Tests ==============

    @Test
    @DisplayName("getUserRoles should return user's roles")
    void getUserRolesShouldReturnUserRoles() {
        // Given
        Set<Role> expectedRoles = Set.of(
            createRole("OWNER", true, null),
            createRole("MANAGER", true, null)
        );
        User user = createUser(TEST_USER_ID, "test@example.com", expectedRoles);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));

        // When
        Set<Role> result = roleService.getUserRoles(TEST_USER_ID);

        // Then
        assertThat(result).hasSize(2)
                .containsAll(expectedRoles);
    }

    @Test
    @DisplayName("getUserRoles should throw exception when user not found")
    void getUserRolesShouldThrowWhenUserNotFound() {
        // Given
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> roleService.getUserRoles(TEST_USER_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found");
    }

    // ============== assignRoleToUser Tests ==============

    @Test
    @DisplayName("assignRoleToUser should assign role by ID")
    void assignRoleToUserShouldAssignRoleById() {
        // Given
        Set<Role> userRoles = new HashSet<>();
        User user = createUser(TEST_USER_ID, "test@example.com", userRoles);
        Role role = createRole("MANAGER", true, null);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));
        when(roleRepository.findById(TEST_ROLE_ID)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        roleService.assignRoleToUser(TEST_USER_ID, TEST_ROLE_ID);

        // Then
        assertThat(user.getRoles()).contains(role);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("assignRoleToUser should assign role by name when ID not found")
    void assignRoleToUserShouldAssignRoleByName() {
        // Given
        Set<Role> userRoles = new HashSet<>();
        User user = createUser(TEST_USER_ID, "test@example.com", userRoles);
        Role role = createRole("MANAGER", true, null);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));
        when(roleRepository.findById("MANAGER")).thenReturn(Optional.empty());
        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        roleService.assignRoleToUser(TEST_USER_ID, "MANAGER");

        // Then
        assertThat(user.getRoles()).contains(role);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("assignRoleToUser should throw exception when user not found")
    void assignRoleToUserShouldThrowWhenUserNotFound() {
        // Given
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> roleService.assignRoleToUser(TEST_USER_ID, TEST_ROLE_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("assignRoleToUser should throw exception when role not found")
    void assignRoleToUserShouldThrowWhenRoleNotFound() {
        // Given
        User user = createUser(TEST_USER_ID, "test@example.com", new HashSet<>());
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));
        when(roleRepository.findById("NONEXISTENT")).thenReturn(Optional.empty());
        when(roleRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> roleService.assignRoleToUser(TEST_USER_ID, "NONEXISTENT"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role not found");
    }

    // ============== removeRoleFromUser Tests ==============

    @Test
    @DisplayName("removeRoleFromUser should remove role from user")
    void removeRoleFromUserShouldRemoveRole() {
        // Given
        Role role = createRole("MANAGER", true, null);
        Set<Role> userRoles = new HashSet<>(Set.of(role));
        User user = createUser(TEST_USER_ID, "test@example.com", userRoles);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));
        when(roleRepository.findById(TEST_ROLE_ID)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        roleService.removeRoleFromUser(TEST_USER_ID, TEST_ROLE_ID);

        // Then
        assertThat(user.getRoles()).doesNotContain(role);
        verify(userRepository).save(user);
    }

    // ============== userHasRole Tests ==============

    @Test
    @DisplayName("userHasRole should return true when user has role")
    void userHasRoleShouldReturnTrueWhenUserHasRole() {
        // Given
        Role role = createRole("MANAGER", true, null);
        User user = createUser(TEST_USER_ID, "test@example.com", Set.of(role));
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));

        // When
        boolean result = roleService.userHasRole(TEST_USER_ID, "MANAGER");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("userHasRole should return false when user does not have role")
    void userHasRoleShouldReturnFalseWhenUserDoesNotHaveRole() {
        // Given
        User user = createUser(TEST_USER_ID, "test@example.com", new HashSet<>());
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));

        // When
        boolean result = roleService.userHasRole(TEST_USER_ID, "MANAGER");

        // Then
        assertThat(result).isFalse();
    }

    // ============== createRole Tests ==============

    @Test
    @DisplayName("createRole should create custom role successfully")
    void createRoleShouldCreateCustomRole() {
        // Given
        RoleCreateRequest request = RoleCreateRequest.builder()
            .name("CUSTOM_ROLE")
            .description("Custom role for testing")
            .build();

        Tenant tenant = Tenant.builder().id(TEST_TENANT_ID).name("Test Tenant").build();
        Role expectedRole = createRole("CUSTOM_ROLE", false, TEST_TENANT_ID);

        when(roleRepository.findByName("CUSTOM_ROLE")).thenReturn(Optional.empty());
        when(tenantRepository.findById(TEST_TENANT_ID)).thenReturn(Optional.of(tenant));
        when(roleRepository.save(any(Role.class))).thenReturn(expectedRole);

        // When
        Role result = roleService.createRole(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSystem()).isFalse();
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("createRole should throw exception when role name already exists")
    void createRoleShouldThrowWhenRoleNameExists() {
        // Given
        RoleCreateRequest request = RoleCreateRequest.builder()
            .name("EXISTING_ROLE")
            .description("Role that already exists")
            .build();

        Role existingRole = createRole("EXISTING_ROLE", false, TEST_TENANT_ID);
        when(roleRepository.findByName("EXISTING_ROLE")).thenReturn(Optional.of(existingRole));

        // When/Then
        assertThatThrownBy(() -> roleService.createRole(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("createRole should throw exception when role name format is invalid")
    void createRoleShouldThrowWhenRoleNameFormatInvalid() {
        // Given
        RoleCreateRequest request = RoleCreateRequest.builder()
            .name("invalid-role-name")
            .description("Invalid role name")
            .build();

        when(roleRepository.findByName("invalid-role-name")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> roleService.createRole(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("uppercase");
    }

    // ============== updateRole Tests ==============

    @Test
    @DisplayName("updateRole should update custom role successfully")
    void updateRoleShouldUpdateCustomRole() {
        // Given
        RoleUpdateRequest request = RoleUpdateRequest.builder()
            .description("Updated description")
            .build();

        Role customRole = createRole("CUSTOM_ROLE", false, TEST_TENANT_ID);
        when(roleRepository.findById(TEST_ROLE_ID)).thenReturn(Optional.of(customRole));
        when(roleRepository.save(any(Role.class))).thenReturn(customRole);

        // When
        Role result = roleService.updateRole(TEST_ROLE_ID, request);

        // Then
        assertThat(result.getDescription()).isEqualTo("Updated description");
        verify(roleRepository).save(customRole);
    }

    @Test
    @DisplayName("updateRole should throw exception when trying to update system role")
    void updateRoleShouldThrowWhenUpdatingSystemRole() {
        // Given
        RoleUpdateRequest request = RoleUpdateRequest.builder()
            .description("Attempt to update system role")
            .build();

        Role systemRole = createRole("OWNER", true, null);
        when(roleRepository.findById(TEST_ROLE_ID)).thenReturn(Optional.of(systemRole));

        // When/Then
        assertThatThrownBy(() -> roleService.updateRole(TEST_ROLE_ID, request))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("System roles cannot be modified");
    }

    // ============== deleteRole Tests ==============

    @Test
    @DisplayName("deleteRole should delete custom role when not in use")
    void deleteRoleShouldDeleteCustomRole() {
        // Given
        Role customRole = Role.builder()
            .id(TEST_ROLE_ID)
            .name("CUSTOM_ROLE")
            .description("Custom role")
            .isSystem(false)
            .users(new HashSet<>())
            .build();

        when(roleRepository.findById(TEST_ROLE_ID)).thenReturn(Optional.of(customRole));

        // When
        roleService.deleteRole(TEST_ROLE_ID);

        // Then
        verify(roleRepository).delete(customRole);
    }

    @Test
    @DisplayName("deleteRole should throw exception when trying to delete system role")
    void deleteRoleShouldThrowWhenDeletingSystemRole() {
        // Given
        Role systemRole = createRole("OWNER", true, null);
        when(roleRepository.findById(TEST_ROLE_ID)).thenReturn(Optional.of(systemRole));

        // When/Then
        assertThatThrownBy(() -> roleService.deleteRole(TEST_ROLE_ID))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("System roles cannot be deleted");
    }

    @Test
    @DisplayName("deleteRole should throw exception when role is assigned to users")
    void deleteRoleShouldThrowWhenRoleInUse() {
        // Given
        User user = createUser("user-1", "user@example.com", new HashSet<>());
        Role customRole = Role.builder()
            .id(TEST_ROLE_ID)
            .name("CUSTOM_ROLE")
            .isSystem(false)
            .users(new HashSet<>(Set.of(user)))
            .build();

        when(roleRepository.findById(TEST_ROLE_ID)).thenReturn(Optional.of(customRole));

        // When/Then
        assertThatThrownBy(() -> roleService.deleteRole(TEST_ROLE_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("assigned to");
    }

    // ============== addPermissionToRole Tests ==============

    @Test
    @DisplayName("addPermissionToRole should add permission to custom role")
    void addPermissionToRoleShouldAddPermission() {
        // Given
        Role customRole = createRole("CUSTOM_ROLE", false, TEST_TENANT_ID);
        customRole.setPermissions(new HashSet<>());
        Permission permission = createPermission("PRODUCT_READ");

        when(roleRepository.findById(TEST_ROLE_ID)).thenReturn(Optional.of(customRole));
        when(permissionRepository.findById(TEST_PERMISSION_ID)).thenReturn(Optional.of(permission));
        when(roleRepository.save(any(Role.class))).thenReturn(customRole);

        // When
        roleService.addPermissionToRole(TEST_ROLE_ID, TEST_PERMISSION_ID);

        // Then
        assertThat(customRole.getPermissions()).contains(permission);
        verify(roleRepository).save(customRole);
    }

    @Test
    @DisplayName("addPermissionToRole should throw exception when modifying system role")
    void addPermissionToRoleShouldThrowWhenModifyingSystemRole() {
        // Given
        Role systemRole = createRole("OWNER", true, null);
        when(roleRepository.findById(TEST_ROLE_ID)).thenReturn(Optional.of(systemRole));

        // When/Then
        assertThatThrownBy(() -> roleService.addPermissionToRole(TEST_ROLE_ID, TEST_PERMISSION_ID))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Cannot modify permissions of system role");
    }

    // ============== removePermissionFromRole Tests ==============

    @Test
    @DisplayName("removePermissionFromRole should remove permission from custom role")
    void removePermissionFromRoleShouldRemovePermission() {
        // Given
        Permission permission = createPermission("PRODUCT_READ");
        Role customRole = createRole("CUSTOM_ROLE", false, TEST_TENANT_ID);
        customRole.setPermissions(new HashSet<>(Set.of(permission)));

        when(roleRepository.findById(TEST_ROLE_ID)).thenReturn(Optional.of(customRole));
        when(permissionRepository.findById(TEST_PERMISSION_ID)).thenReturn(Optional.of(permission));
        when(roleRepository.save(any(Role.class))).thenReturn(customRole);

        // When
        roleService.removePermissionFromRole(TEST_ROLE_ID, TEST_PERMISSION_ID);

        // Then
        assertThat(customRole.getPermissions()).doesNotContain(permission);
        verify(roleRepository).save(customRole);
    }

    @Test
    @DisplayName("removePermissionFromRole should throw exception when modifying system role")
    void removePermissionFromRoleShouldThrowWhenModifyingSystemRole() {
        // Given
        Role systemRole = createRole("OWNER", true, null);
        when(roleRepository.findById(TEST_ROLE_ID)).thenReturn(Optional.of(systemRole));

        // When/Then
        assertThatThrownBy(() -> roleService.removePermissionFromRole(TEST_ROLE_ID, TEST_PERMISSION_ID))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Cannot modify permissions of system role");
    }

    // Helper methods

    private Role createRole(String name, boolean isSystem, String tenantId) {
        return Role.builder()
            .id(UUID.randomUUID().toString())
            .name(name)
            .description("Test role: " + name)
            .isSystem(isSystem)
            .tenant(tenantId != null ? Tenant.builder().id(tenantId).build() : null)
            .permissions(new HashSet<>())
            .users(new HashSet<>())
            .build();
    }

    private User createUser(String id, String email, Set<Role> roles) {
        return User.builder()
            .id(id)
            .email(email)
            .roles(roles)
            .tenant(Tenant.builder().id(TEST_TENANT_ID).build())
            .build();
    }

    private Permission createPermission(String name) {
        return Permission.builder()
            .id(UUID.randomUUID().toString())
            .name(name)
            .description("Test permission: " + name)
            .resource("PRODUCT")
            .build();
    }
}
