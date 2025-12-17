package com.princely.shopmanager.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.core.dto.RoleAssignmentRequest;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.test.TestConstants;
import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import com.princely.shopmanager.test.security.WithMockPermissions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for RoleController.
 * Tests granular permission-based authorization for role management.
 */
@DisplayName("Role Controller Integration Tests")
@Disabled("Temporarily disabled during IT reduction - will be replaced with minimal happy path test and unit tests")
class RoleControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    // ============== Get All Roles Tests ==============

    @Test
    @DisplayName("Should return all roles when user has ROLE_LIST permission")
    @WithMockPermissions(role = "MANAGER")
    void shouldReturnAllRolesWithPermission() throws Exception {
        mockMvc.perform(get("/api/roles"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", isA(Iterable.class)))
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4)))); // At least SYSTEM_ADMIN, OWNER, MANAGER, EMPLOYEE
    }

    @Test
    @DisplayName("Should deny access to roles list without ROLE_LIST permission")
    @WithMockPermissions(value = {"PRODUCT_READ"}) // Has product permission but not role permission
    void shouldDenyAccessToRolesListWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/roles"))
            .andExpect(status().isForbidden());
    }

    // ============== Get Role by ID Tests ==============

    @Test
    @DisplayName("Should return role details with permissions when user has ROLE_READ permission")
    @WithMockPermissions(role = "OWNER")
    void shouldReturnRoleByIdWithPermissions() throws Exception {
        mockMvc.perform(get("/api/roles/{roleId}", "manager-role-id"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value("manager-role-id"))
            .andExpect(jsonPath("$.name").value("MANAGER"))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.permissions", isA(Iterable.class)))
            .andExpect(jsonPath("$.permissions", hasSize(greaterThan(0)))); // Should have permissions
    }

    @Test
    @DisplayName("Should deny access to role details without ROLE_READ permission")
    @WithMockPermissions(value = {"SALES_CREATE"})
    void shouldDenyAccessToRoleDetailsWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/roles/{roleId}", "manager-role-id"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 when role not found")
    @WithMockPermissions(role = "OWNER")
    void shouldReturn404WhenRoleNotFound() throws Exception {
        mockMvc.perform(get("/api/roles/{roleId}", "nonexistent-role"))
            .andExpect(status().isNotFound());
    }

    // ============== Get User Roles Tests ==============

    @Test
    @DisplayName("Should return user roles when user has ROLE_LIST permission")
    @WithMockPermissions(role = "OWNER")
    void shouldReturnUserRoles() throws Exception {
        // Use existing test user from test-data.sql
        String userId = TestConstants.MOCK_USER_ID;

        mockMvc.perform(get("/api/users/{userId}/roles", userId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", isA(Iterable.class)));
    }

    @Test
    @DisplayName("Should deny access to user roles without ROLE_LIST permission")
    @WithMockPermissions(value = {"INVENTORY_READ"})
    void shouldDenyAccessToUserRolesWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/roles", TestConstants.MOCK_USER_ID))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 when user not found for roles")
    @WithMockPermissions(role = "OWNER")
    void shouldReturn404WhenUserNotFoundForRoles() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/roles", "nonexistent-user"))
            .andExpect(status().isNotFound());
    }

    // ============== Assign Role to User Tests ==============

    @Test
    @DisplayName("Should assign role to user when user has ROLE_ASSIGN permission")
    @WithMockPermissions(role = "OWNER")
    void shouldAssignRoleToUser() throws Exception {
        String userId = TestConstants.MOCK_USER_ID;
        RoleAssignmentRequest request = new RoleAssignmentRequest("EMPLOYEE");

        mockMvc.perform(post("/api/users/{userId}/roles", userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should deny role assignment without ROLE_ASSIGN permission")
    @WithMockPermissions(role = "EMPLOYEE")
    void shouldDenyRoleAssignmentWithoutPermission() throws Exception {
        String userId = TestConstants.MOCK_USER_ID;
        RoleAssignmentRequest request = new RoleAssignmentRequest("MANAGER");

        mockMvc.perform(post("/api/users/{userId}/roles", userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 when assigning role to non-existent user")
    @WithMockPermissions(role = "OWNER")
    void shouldReturn404WhenAssigningRoleToNonExistentUser() throws Exception {
        RoleAssignmentRequest request = new RoleAssignmentRequest("EMPLOYEE");

        mockMvc.perform(post("/api/users/{userId}/roles", "nonexistent-user")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when assigning non-existent role")
    @WithMockPermissions(role = "OWNER")
    void shouldReturn400WhenAssigningNonExistentRole() throws Exception {
        String userId = TestConstants.MOCK_USER_ID;
        RoleAssignmentRequest request = new RoleAssignmentRequest("NONEXISTENT_ROLE");

        mockMvc.perform(post("/api/users/{userId}/roles", userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    // ============== Remove Role from User Tests ==============

    @Test
    @DisplayName("Should remove role from user when user has ROLE_ASSIGN permission")
    @WithMockPermissions(role = "OWNER")
    void shouldRemoveRoleFromUser() throws Exception {
        String userId = TestConstants.MOCK_USER_ID;
        String roleId = "employee-role-id";

        mockMvc.perform(delete("/api/users/{userId}/roles/{roleId}", userId, roleId)
                .with(csrf()))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should deny role removal without ROLE_ASSIGN permission")
    @WithMockPermissions(role = "EMPLOYEE")
    void shouldDenyRoleRemovalWithoutPermission() throws Exception {
        String userId = TestConstants.MOCK_USER_ID;
        String roleId = "manager-role-id";

        mockMvc.perform(delete("/api/users/{userId}/roles/{roleId}", userId, roleId)
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 when removing role from non-existent user")
    @WithMockPermissions(role = "OWNER")
    void shouldReturn404WhenRemovingRoleFromNonExistentUser() throws Exception {
        mockMvc.perform(delete("/api/users/{userId}/roles/{roleId}", "nonexistent-user", "manager-role-id")
                .with(csrf()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when removing non-existent role")
    @WithMockPermissions(role = "OWNER")
    void shouldReturn404WhenRemovingNonExistentRole() throws Exception {
        String userId = TestConstants.MOCK_USER_ID;

        mockMvc.perform(delete("/api/users/{userId}/roles/{roleId}", userId, "nonexistent-role")
                .with(csrf()))
            .andExpect(status().isNotFound());
    }

    // ============== Permission Tests ==============

    @Test
    @DisplayName("Should include permissions in role response")
    @WithMockPermissions(role = "OWNER")
    void shouldIncludePermissionsInRoleResponse() throws Exception {
        mockMvc.perform(get("/api/roles/{roleId}", "owner-role-id"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.permissions", isA(Iterable.class)))
            .andExpect(jsonPath("$.permissions", hasSize(greaterThan(10)))) // Owner should have many permissions
            .andExpect(jsonPath("$.permissions", hasItem("SHOP_CREATE")))
            .andExpect(jsonPath("$.permissions", hasItem("PRODUCT_CREATE")))
            .andExpect(jsonPath("$.permissions", hasItem("SALES_CREATE")));
    }

    @Test
    @DisplayName("MANAGER role should have appropriate permissions")
    @WithMockPermissions(role = "OWNER")
    void managerShouldHaveAppropriatePermissions() throws Exception {
        mockMvc.perform(get("/api/roles/{roleId}", "manager-role-id"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.permissions", hasItem("PRODUCT_CREATE")))
            .andExpect(jsonPath("$.permissions", hasItem("INVENTORY_CREATE")))
            .andExpect(jsonPath("$.permissions", hasItem("SALES_CREATE")))
            .andExpect(jsonPath("$.permissions", not(hasItem("SHOP_DELETE")))); // Manager should not have shop delete
    }

    @Test
    @DisplayName("EMPLOYEE role should have limited permissions")
    @WithMockPermissions(role = "OWNER")
    void employeeShouldHaveLimitedPermissions() throws Exception {
        mockMvc.perform(get("/api/roles/{roleId}", "employee-role-id"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.permissions", hasItem("SALES_CREATE")))
            .andExpect(jsonPath("$.permissions", hasItem("PRODUCT_READ")))
            .andExpect(jsonPath("$.permissions", not(hasItem("PRODUCT_DELETE")))) // Employee cannot delete products
            .andExpect(jsonPath("$.permissions", not(hasItem("USER_CREATE")))); // Employee cannot create users
    }

    @Test
    @DisplayName("SYSTEM_ADMIN role should have all permissions")
    @WithMockPermissions(role = "SYSTEM_ADMIN")
    void systemAdminShouldHaveAllPermissions() throws Exception {
        mockMvc.perform(get("/api/roles/{roleId}", "super-admin-role-id"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.permissions", hasSize(greaterThan(50)))) // System admin has many permissions
            .andExpect(jsonPath("$.permissions", hasItem("TENANT_CREATE")))
            .andExpect(jsonPath("$.permissions", hasItem("SHOP_DELETE")))
            .andExpect(jsonPath("$.permissions", hasItem("USER_DELETE")));
    }

    // ============== Create Custom Role Tests ==============

    @Test
    @DisplayName("OWNER should create custom role")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldCreateCustomRole() throws Exception {
        String createRequest = """
            {
                "name": "CUSTOM_SUPERVISOR",
                "description": "Supervisor with custom permissions",
                "permissionNames": ["PRODUCT_READ", "PRODUCT_LIST", "SALES_CREATE", "INVENTORY_READ"]
            }
            """;

        mockMvc.perform(post("/api/roles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("CUSTOM_SUPERVISOR"))
            .andExpect(jsonPath("$.description").value("Supervisor with custom permissions"))
            .andExpect(jsonPath("$.isSystem").value(false))
            .andExpect(jsonPath("$.permissions", hasSize(4)));
    }

    @Test
    @DisplayName("MANAGER should NOT create custom role")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldNotCreateCustomRole() throws Exception {
        String createRequest = """
            {
                "name": "TEST_ROLE",
                "description": "Test role",
                "permissionNames": ["PRODUCT_READ"]
            }
            """;

        mockMvc.perform(post("/api/roles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 400 when creating role with duplicate name")
    @WithMockPermissions(role = "OWNER")
    void shouldReturn400WhenCreatingRoleWithDuplicateName() throws Exception {
        String createRequest = """
            {
                "name": "MANAGER",
                "description": "Duplicate manager role",
                "permissionNames": ["PRODUCT_READ"]
            }
            """;

        mockMvc.perform(post("/api/roles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
            .andExpect(status().isBadRequest());
    }

    // ============== Update Custom Role Tests ==============

    @Test
    @DisplayName("OWNER should update custom role")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldUpdateCustomRole() throws Exception {
        // First create a custom role
        String createRequest = """
            {
                "name": "UPDATABLE_ROLE",
                "description": "Role to be updated",
                "permissionNames": ["PRODUCT_READ"]
            }
            """;

        String createResponse = mockMvc.perform(post("/api/roles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String roleId = objectMapper.readTree(createResponse).get("id").asText();

        // Update the role
        String updateRequest = """
            {
                "description": "Updated description",
                "permissionNames": ["PRODUCT_READ", "PRODUCT_LIST", "SALES_READ"]
            }
            """;

        mockMvc.perform(put("/api/roles/{roleId}", roleId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Updated description"))
            .andExpect(jsonPath("$.permissions", hasSize(3)));
    }

    @Test
    @DisplayName("Should return 400 when updating system role")
    @WithMockPermissions(role = "OWNER")
    void shouldReturn400WhenUpdatingSystemRole() throws Exception {
        String updateRequest = """
            {
                "description": "Trying to update system role",
                "permissionNames": ["PRODUCT_READ"]
            }
            """;

        mockMvc.perform(put("/api/roles/{roleId}", "owner-role-id")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("MANAGER should NOT update role")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldNotUpdateRole() throws Exception {
        String updateRequest = """
            {
                "description": "Test update",
                "permissionNames": ["PRODUCT_READ"]
            }
            """;

        mockMvc.perform(put("/api/roles/{roleId}", "any-role-id")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
            .andExpect(status().isForbidden());
    }

    // ============== Delete Custom Role Tests ==============

    @Test
    @DisplayName("OWNER should delete custom role")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldDeleteCustomRole() throws Exception {
        // Create a custom role
        String createRequest = """
            {
                "name": "DELETABLE_ROLE",
                "description": "Role to be deleted",
                "permissionNames": ["PRODUCT_READ"]
            }
            """;

        String createResponse = mockMvc.perform(post("/api/roles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String roleId = objectMapper.readTree(createResponse).get("id").asText();

        // Delete the role
        mockMvc.perform(delete("/api/roles/{roleId}", roleId)
                .with(csrf()))
            .andExpect(status().isNoContent());

        // Verify role is deleted
        mockMvc.perform(get("/api/roles/{roleId}", roleId))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when deleting system role")
    @WithMockPermissions(role = "OWNER")
    void shouldReturn400WhenDeletingSystemRole() throws Exception {
        mockMvc.perform(delete("/api/roles/{roleId}", "owner-role-id")
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when deleting role assigned to users")
    @WithMockPermissions(role = "OWNER")
    void shouldReturn400WhenDeletingRoleAssignedToUsers() throws Exception {
        // Create and assign a role to a user
        String createRequest = """
            {
                "name": "ASSIGNED_ROLE",
                "description": "Role assigned to users",
                "permissionNames": ["PRODUCT_READ"]
            }
            """;

        String createResponse = mockMvc.perform(post("/api/roles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String roleId = objectMapper.readTree(createResponse).get("id").asText();

        // Assign role to user
        String assignRequest = String.format("{\"roleName\": \"ASSIGNED_ROLE\"}");
        mockMvc.perform(post("/api/users/{userId}/roles", TestConstants.MOCK_USER_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(assignRequest))
            .andExpect(status().isNoContent());

        // Try to delete role
        mockMvc.perform(delete("/api/roles/{roleId}", roleId)
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("MANAGER should NOT delete role")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldNotDeleteRole() throws Exception {
        mockMvc.perform(delete("/api/roles/{roleId}", "any-role-id")
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    // ============== Add Permission to Role Tests ==============

    @Test
    @DisplayName("OWNER should add permission to custom role")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldAddPermissionToRole() throws Exception {
        // Create custom role
        String createRequest = """
            {
                "name": "PERMISSION_TEST_ROLE",
                "description": "Testing permission management",
                "permissionNames": ["PRODUCT_READ"]
            }
            """;

        String createResponse = mockMvc.perform(post("/api/roles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String roleId = objectMapper.readTree(createResponse).get("id").asText();

        // Add permission
        mockMvc.perform(post("/api/roles/{roleId}/permissions/{permissionName}", roleId, "SALES_CREATE")
                .with(csrf()))
            .andExpect(status().isNoContent());

        // Verify permission was added
        mockMvc.perform(get("/api/roles/{roleId}", roleId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.permissions", hasItem("SALES_CREATE")))
            .andExpect(jsonPath("$.permissions", hasSize(2)));
    }

    @Test
    @DisplayName("Should return 400 when adding permission to system role")
    @WithMockPermissions(role = "OWNER")
    void shouldReturn400WhenAddingPermissionToSystemRole() throws Exception {
        mockMvc.perform(post("/api/roles/{roleId}/permissions/{permissionName}", "owner-role-id", "SOME_PERMISSION")
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("MANAGER should NOT add permission to role")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldNotAddPermissionToRole() throws Exception {
        mockMvc.perform(post("/api/roles/{roleId}/permissions/{permissionName}", "any-role-id", "PRODUCT_READ")
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    // ============== Remove Permission from Role Tests ==============

    @Test
    @DisplayName("OWNER should remove permission from custom role")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldRemovePermissionFromRole() throws Exception {
        // Create custom role with multiple permissions
        String createRequest = """
            {
                "name": "REMOVE_PERM_ROLE",
                "description": "Testing permission removal",
                "permissionNames": ["PRODUCT_READ", "SALES_CREATE", "INVENTORY_READ"]
            }
            """;

        String createResponse = mockMvc.perform(post("/api/roles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String roleId = objectMapper.readTree(createResponse).get("id").asText();

        // Remove permission
        mockMvc.perform(delete("/api/roles/{roleId}/permissions/{permissionName}", roleId, "SALES_CREATE")
                .with(csrf()))
            .andExpect(status().isNoContent());

        // Verify permission was removed
        mockMvc.perform(get("/api/roles/{roleId}", roleId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.permissions", not(hasItem("SALES_CREATE"))))
            .andExpect(jsonPath("$.permissions", hasSize(2)));
    }

    @Test
    @DisplayName("Should return 400 when removing permission from system role")
    @WithMockPermissions(role = "OWNER")
    void shouldReturn400WhenRemovingPermissionFromSystemRole() throws Exception {
        mockMvc.perform(delete("/api/roles/{roleId}/permissions/{permissionName}", "owner-role-id", "PRODUCT_READ")
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("MANAGER should NOT remove permission from role")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldNotRemovePermissionFromRole() throws Exception {
        mockMvc.perform(delete("/api/roles/{roleId}/permissions/{permissionName}", "any-role-id", "PRODUCT_READ")
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    // ============== Bulk Update Role Permissions Tests ==============

    @Test
    @DisplayName("OWNER should bulk update role permissions")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldBulkUpdateRolePermissions() throws Exception {
        // Create custom role
        String createRequest = """
            {
                "name": "BULK_UPDATE_ROLE",
                "description": "Testing bulk permission update",
                "permissionNames": ["PRODUCT_READ", "SALES_CREATE"]
            }
            """;

        String createResponse = mockMvc.perform(post("/api/roles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String roleId = objectMapper.readTree(createResponse).get("id").asText();

        // Bulk update permissions
        String bulkUpdateRequest = """
            {
                "permissionNames": ["INVENTORY_READ", "INVENTORY_LIST", "EXPENSE_READ"]
            }
            """;

        mockMvc.perform(put("/api/roles/{roleId}/permissions", roleId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(bulkUpdateRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.permissions", hasSize(3)))
            .andExpect(jsonPath("$.permissions", hasItem("INVENTORY_READ")))
            .andExpect(jsonPath("$.permissions", hasItem("INVENTORY_LIST")))
            .andExpect(jsonPath("$.permissions", hasItem("EXPENSE_READ")))
            .andExpect(jsonPath("$.permissions", not(hasItem("PRODUCT_READ"))))
            .andExpect(jsonPath("$.permissions", not(hasItem("SALES_CREATE"))));
    }

    @Test
    @DisplayName("Should return 400 when bulk updating permissions for system role")
    @WithMockPermissions(role = "OWNER")
    void shouldReturn400WhenBulkUpdatingPermissionsForSystemRole() throws Exception {
        String bulkUpdateRequest = """
            {
                "permissionNames": ["PRODUCT_READ"]
            }
            """;

        mockMvc.perform(put("/api/roles/{roleId}/permissions", "owner-role-id")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(bulkUpdateRequest))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("MANAGER should NOT bulk update role permissions")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldNotBulkUpdateRolePermissions() throws Exception {
        String bulkUpdateRequest = """
            {
                "permissionNames": ["PRODUCT_READ"]
            }
            """;

        mockMvc.perform(put("/api/roles/{roleId}/permissions", "any-role-id")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(bulkUpdateRequest))
            .andExpect(status().isForbidden());
    }
}
