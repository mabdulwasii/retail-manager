package com.princely.shopmanager.core.service;

import com.princely.shopmanager.core.domain.Permission;
import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.dto.PermissionGroupResponse;
import com.princely.shopmanager.core.dto.PermissionResponse;
import com.princely.shopmanager.core.repository.PermissionRepository;
import com.princely.shopmanager.core.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PermissionService.
 * Tests permission query and grouping logic without Spring context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService Unit Tests")
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private PermissionService permissionService;

    private static final String TEST_ROLE_ID = "test-role";

    // ============== getAllPermissions Tests ==============

    @Test
    @DisplayName("getAllPermissions should return all permissions")
    void getAllPermissionsShouldReturnAllPermissions() {
        // Given
        List<Permission> expectedPermissions = List.of(
            createPermission("PRODUCT_CREATE", "PRODUCT"),
            createPermission("PRODUCT_READ", "PRODUCT"),
            createPermission("SALES_CREATE", "SALES")
        );
        when(permissionRepository.findAll()).thenReturn(expectedPermissions);

        // When
        List<Permission> result = permissionService.getAllPermissions();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsAll(expectedPermissions);
        verify(permissionRepository).findAll();
    }

    // ============== getPermissionsGroupedByResource Tests ==============

    @Test
    @DisplayName("getPermissionsGroupedByResource should group permissions by resource")
    void getPermissionsGroupedByResourceShouldGroupPermissions() {
        // Given
        List<Permission> permissions = List.of(
            createPermission("PRODUCT_CREATE", "PRODUCT"),
            createPermission("PRODUCT_READ", "PRODUCT"),
            createPermission("SALES_CREATE", "SALES"),
            createPermission("SALES_READ", "SALES")
        );
        when(permissionRepository.findAll()).thenReturn(permissions);

        // When
        Map<String, List<PermissionResponse>> result = permissionService.getPermissionsGroupedByResource();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsKeys("PRODUCT", "SALES");
        assertThat(result.get("PRODUCT")).hasSize(2);
        assertThat(result.get("SALES")).hasSize(2);
    }

    // ============== getPermissionGroupsAsResponse Tests ==============

    @Test
    @DisplayName("getPermissionGroupsAsResponse should return sorted permission groups")
    void getPermissionGroupsAsResponseShouldReturnSortedGroups() {
        // Given
        List<Permission> permissions = List.of(
            createPermission("PRODUCT_CREATE", "PRODUCT"),
            createPermission("SALES_CREATE", "SALES"),
            createPermission("INVENTORY_CREATE", "INVENTORY")
        );
        when(permissionRepository.findAll()).thenReturn(permissions);

        // When
        List<PermissionGroupResponse> result = permissionService.getPermissionGroupsAsResponse();

        // Then
        assertThat(result).hasSize(3);
        // Should be sorted alphabetically
        assertThat(result.get(0).getResource()).isEqualTo("INVENTORY");
        assertThat(result.get(1).getResource()).isEqualTo("PRODUCT");
        assertThat(result.get(2).getResource()).isEqualTo("SALES");
        // Each group should have count
        assertThat(result.get(0).getCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("getPermissionGroupsAsResponse should group multiple permissions per resource")
    void getPermissionGroupsAsResponseShouldGroupMultiplePermissions() {
        // Given
        List<Permission> permissions = List.of(
            createPermission("PRODUCT_CREATE", "PRODUCT"),
            createPermission("PRODUCT_READ", "PRODUCT"),
            createPermission("PRODUCT_UPDATE", "PRODUCT"),
            createPermission("SALES_CREATE", "SALES")
        );
        when(permissionRepository.findAll()).thenReturn(permissions);

        // When
        List<PermissionGroupResponse> result = permissionService.getPermissionGroupsAsResponse();

        // Then
        assertThat(result).hasSize(2);
        PermissionGroupResponse productGroup = result.stream()
            .filter(g -> g.getResource().equals("PRODUCT"))
            .findFirst()
            .orElseThrow();
        assertThat(productGroup.getCount()).isEqualTo(3);
        assertThat(productGroup.getPermissions()).hasSize(3);
    }

    // ============== getPermissionsByRole Tests ==============

    @Test
    @DisplayName("getPermissionsByRole should return role permissions")
    void getPermissionsByRoleShouldReturnPermissions() {
        // Given
        Set<Permission> rolePermissions = Set.of(
            createPermission("PRODUCT_CREATE", "PRODUCT"),
            createPermission("PRODUCT_READ", "PRODUCT")
        );
        Role role = Role.builder()
            .id(TEST_ROLE_ID)
            .name("MANAGER")
            .permissions(rolePermissions)
            .build();

        when(roleRepository.findById(TEST_ROLE_ID)).thenReturn(Optional.of(role));

        // When
        Set<Permission> result = permissionService.getPermissionsByRole(TEST_ROLE_ID);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsAll(rolePermissions);
    }

    @Test
    @DisplayName("getPermissionsByRole should throw exception when role not found")
    void getPermissionsByRoleShouldThrowWhenRoleNotFound() {
        // Given
        when(roleRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> permissionService.getPermissionsByRole("nonexistent"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role not found");
    }

    // ============== getPermissionByName Tests ==============

    @Test
    @DisplayName("getPermissionByName should return permission when found")
    void getPermissionByNameShouldReturnPermission() {
        // Given
        Permission expectedPermission = createPermission("PRODUCT_CREATE", "PRODUCT");
        when(permissionRepository.findByName("PRODUCT_CREATE")).thenReturn(Optional.of(expectedPermission));

        // When
        Permission result = permissionService.getPermissionByName("PRODUCT_CREATE");

        // Then
        assertThat(result).isEqualTo(expectedPermission);
        verify(permissionRepository).findByName("PRODUCT_CREATE");
    }

    @Test
    @DisplayName("getPermissionByName should throw exception when not found")
    void getPermissionByNameShouldThrowWhenNotFound() {
        // Given
        when(permissionRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> permissionService.getPermissionByName("NONEXISTENT"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Permission not found");
    }

    // ============== getPermissionByIdentifier Tests ==============

    @Test
    @DisplayName("getPermissionByIdentifier should find permission by ID")
    void getPermissionByIdentifierShouldFindById() {
        // Given
        Permission expectedPermission = createPermission("PRODUCT_CREATE", "PRODUCT");
        when(permissionRepository.findById("perm-123")).thenReturn(Optional.of(expectedPermission));

        // When
        Permission result = permissionService.getPermissionByIdentifier("perm-123");

        // Then
        assertThat(result).isEqualTo(expectedPermission);
        verify(permissionRepository).findById("perm-123");
    }

    @Test
    @DisplayName("getPermissionByIdentifier should find permission by name when ID not found")
    void getPermissionByIdentifierShouldFindByNameWhenIdNotFound() {
        // Given
        Permission expectedPermission = createPermission("PRODUCT_CREATE", "PRODUCT");
        when(permissionRepository.findById("PRODUCT_CREATE")).thenReturn(Optional.empty());
        when(permissionRepository.findByName("PRODUCT_CREATE")).thenReturn(Optional.of(expectedPermission));

        // When
        Permission result = permissionService.getPermissionByIdentifier("PRODUCT_CREATE");

        // Then
        assertThat(result).isEqualTo(expectedPermission);
        verify(permissionRepository).findById("PRODUCT_CREATE");
        verify(permissionRepository).findByName("PRODUCT_CREATE");
    }

    @Test
    @DisplayName("getPermissionByIdentifier should throw exception when not found by ID or name")
    void getPermissionByIdentifierShouldThrowWhenNotFound() {
        // Given
        when(permissionRepository.findById("NONEXISTENT")).thenReturn(Optional.empty());
        when(permissionRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> permissionService.getPermissionByIdentifier("NONEXISTENT"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Permission not found");
    }

    // ============== getPermissionsByResource Tests ==============

    @Test
    @DisplayName("getPermissionsByResource should return permissions for resource")
    void getPermissionsByResourceShouldReturnPermissions() {
        // Given
        List<Permission> productPermissions = List.of(
            createPermission("PRODUCT_CREATE", "PRODUCT"),
            createPermission("PRODUCT_READ", "PRODUCT")
        );
        when(permissionRepository.findByResource("PRODUCT")).thenReturn(productPermissions);

        // When
        List<Permission> result = permissionService.getPermissionsByResource("product");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsAll(productPermissions);
        verify(permissionRepository).findByResource("PRODUCT"); // Should uppercase
    }

    @Test
    @DisplayName("getPermissionsByResource should uppercase resource name")
    void getPermissionsByResourceShouldUppercaseResourceName() {
        // Given
        when(permissionRepository.findByResource("PRODUCT")).thenReturn(List.of());

        // When
        permissionService.getPermissionsByResource("product");

        // Then
        verify(permissionRepository).findByResource("PRODUCT");
    }

    // ============== permissionExists Tests ==============

    @Test
    @DisplayName("permissionExists should return true when permission exists")
    void permissionExistsShouldReturnTrueWhenExists() {
        // Given
        Permission permission = createPermission("PRODUCT_CREATE", "PRODUCT");
        when(permissionRepository.findByName("PRODUCT_CREATE")).thenReturn(Optional.of(permission));

        // When
        boolean result = permissionService.permissionExists("PRODUCT_CREATE");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("permissionExists should return false when permission does not exist")
    void permissionExistsShouldReturnFalseWhenNotExists() {
        // Given
        when(permissionRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

        // When
        boolean result = permissionService.permissionExists("NONEXISTENT");

        // Then
        assertThat(result).isFalse();
    }

    // ============== getAllResources Tests ==============

    @Test
    @DisplayName("getAllResources should return distinct sorted resource names")
    void getAllResourcesShouldReturnDistinctSortedNames() {
        // Given
        List<Permission> permissions = List.of(
            createPermission("PRODUCT_CREATE", "PRODUCT"),
            createPermission("PRODUCT_READ", "PRODUCT"),
            createPermission("SALES_CREATE", "SALES"),
            createPermission("INVENTORY_CREATE", "INVENTORY"),
            createPermission("INVENTORY_READ", "INVENTORY")
        );
        when(permissionRepository.findAll()).thenReturn(permissions);

        // When
        List<String> result = permissionService.getAllResources();

        // Then
        assertThat(result).hasSize(3); // Distinct
        assertThat(result).containsExactly("INVENTORY", "PRODUCT", "SALES"); // Sorted
    }

    @Test
    @DisplayName("getAllResources should handle empty permissions")
    void getAllResourcesShouldHandleEmptyPermissions() {
        // Given
        when(permissionRepository.findAll()).thenReturn(List.of());

        // When
        List<String> result = permissionService.getAllResources();

        // Then
        assertThat(result).isEmpty();
    }

    // Helper methods

    private Permission createPermission(String name, String resource) {
        return Permission.builder()
            .id(UUID.randomUUID().toString())
            .name(name)
            .description("Test permission: " + name)
            .resource(resource)
            .build();
    }
}
