package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import com.princely.shopmanager.test.security.WithMockPermissions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PermissionController.
 * Tests granular permission-based authorization for permission management.
 */
@DisplayName("Permission Controller Integration Tests")
class PermissionControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ============== List All Permissions Tests ==============

    @Test
    @DisplayName("OWNER should list all permissions")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldListAllPermissions() throws Exception {
        mockMvc.perform(get("/api/permissions"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", isA(Iterable.class)))
            .andExpect(jsonPath("$", hasSize(greaterThan(50)))) // Should have many permissions
            .andExpect(jsonPath("$[0].name").exists())
            .andExpect(jsonPath("$[0].description").exists());
    }

    @Test
    @DisplayName("MANAGER should list all permissions")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldListAllPermissions() throws Exception {
        mockMvc.perform(get("/api/permissions"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", isA(Iterable.class)));
    }

    @Test
    @DisplayName("SYSTEM_ADMIN should list all permissions")
    @WithMockPermissions(role = "SYSTEM_ADMIN")
    void systemAdminShouldListAllPermissions() throws Exception {
        mockMvc.perform(get("/api/permissions"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(greaterThan(90)))); // System admin sees all permissions
    }

    @Test
    @DisplayName("User without PERMISSION_LIST should NOT list permissions")
    @WithMockPermissions(value = {"PRODUCT_READ", "SALES_CREATE"}) // Has other permissions but not PERMISSION_LIST
    void userWithoutPermissionShouldNotListPermissions() throws Exception {
        mockMvc.perform(get("/api/permissions"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("EMPLOYEE should NOT list permissions")
    @WithMockPermissions(role = "EMPLOYEE")
    void employeeShouldNotListPermissions() throws Exception {
        mockMvc.perform(get("/api/permissions"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("INVESTOR should NOT list permissions")
    @WithMockPermissions(role = "INVESTOR")
    void investorShouldNotListPermissions() throws Exception {
        mockMvc.perform(get("/api/permissions"))
            .andExpect(status().isForbidden());
    }

    // ============== Grouped Permissions Tests ==============

    @Test
    @DisplayName("OWNER should get grouped permissions")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldGetGroupedPermissions() throws Exception {
        mockMvc.perform(get("/api/permissions/grouped"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", isA(Object.class)))
            .andExpect(jsonPath("$.PRODUCT", isA(Iterable.class)))
            .andExpect(jsonPath("$.SALES", isA(Iterable.class)))
            .andExpect(jsonPath("$.INVENTORY", isA(Iterable.class)))
            .andExpect(jsonPath("$.USER", isA(Iterable.class)))
            .andExpect(jsonPath("$.ROLE", isA(Iterable.class)));
    }

    @Test
    @DisplayName("MANAGER should get grouped permissions")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldGetGroupedPermissions() throws Exception {
        mockMvc.perform(get("/api/permissions/grouped"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", isA(Object.class)));
    }

    @Test
    @DisplayName("SYSTEM_ADMIN should get all grouped permissions")
    @WithMockPermissions(role = "SYSTEM_ADMIN")
    void systemAdminShouldGetGroupedPermissions() throws Exception {
        mockMvc.perform(get("/api/permissions/grouped"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.TENANT", isA(Iterable.class))) // System admin sees tenant permissions
            .andExpect(jsonPath("$.SHOP", isA(Iterable.class)))
            .andExpect(jsonPath("$.PRODUCT", isA(Iterable.class)))
            .andExpect(jsonPath("$.INVESTMENT", isA(Iterable.class)))
            .andExpect(jsonPath("$.ANALYTICS", isA(Iterable.class)));
    }

    @Test
    @DisplayName("User without PERMISSION_LIST should NOT get grouped permissions")
    @WithMockPermissions(value = {"INVENTORY_READ"})
    void userWithoutPermissionShouldNotGetGroupedPermissions() throws Exception {
        mockMvc.perform(get("/api/permissions/grouped"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("EMPLOYEE should NOT get grouped permissions")
    @WithMockPermissions(role = "EMPLOYEE")
    void employeeShouldNotGetGroupedPermissions() throws Exception {
        mockMvc.perform(get("/api/permissions/grouped"))
            .andExpect(status().isForbidden());
    }

    // ============== Permission Content Validation Tests ==============

    @Test
    @DisplayName("Should include core permission categories in grouped response")
    @WithMockPermissions(role = "OWNER")
    void shouldIncludeCorePermissionCategories() throws Exception {
        mockMvc.perform(get("/api/permissions/grouped"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.PRODUCT").exists())
            .andExpect(jsonPath("$.SALES").exists())
            .andExpect(jsonPath("$.INVENTORY").exists())
            .andExpect(jsonPath("$.EXPENSE").exists())
            .andExpect(jsonPath("$.USER").exists())
            .andExpect(jsonPath("$.ROLE").exists())
            .andExpect(jsonPath("$.SHOP").exists())
            .andExpect(jsonPath("$.CATEGORY").exists())
            .andExpect(jsonPath("$.INVESTMENT").exists())
            .andExpect(jsonPath("$.RETURN").exists())
            .andExpect(jsonPath("$.RECEIPT").exists())
            .andExpect(jsonPath("$.AUDIT_LOG").exists())
            .andExpect(jsonPath("$.ANALYTICS").exists())
            .andExpect(jsonPath("$.FRAUD").exists());
    }

    @Test
    @DisplayName("Should include CRUD operations in PRODUCT permissions")
    @WithMockPermissions(role = "OWNER")
    void shouldIncludeCrudOperationsInProductPermissions() throws Exception {
        mockMvc.perform(get("/api/permissions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name == 'PRODUCT_CREATE')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'PRODUCT_READ')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'PRODUCT_UPDATE')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'PRODUCT_DELETE')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'PRODUCT_LIST')]").exists());
    }

    @Test
    @DisplayName("Should include hierarchical permissions (TENANT, SHOP, etc)")
    @WithMockPermissions(role = "SYSTEM_ADMIN")
    void shouldIncludeHierarchicalPermissions() throws Exception {
        mockMvc.perform(get("/api/permissions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name == 'TENANT_CREATE')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'TENANT_READ')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'SHOP_CREATE')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'SHOP_READ')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'SHOP_LIST')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'SHOP_LIST_ALL')]").exists());
    }

    @Test
    @DisplayName("Should include investment permissions")
    @WithMockPermissions(role = "OWNER")
    void shouldIncludeInvestmentPermissions() throws Exception {
        mockMvc.perform(get("/api/permissions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name == 'INVESTMENT_CREATE')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'INVESTMENT_READ')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'INVESTMENT_LIST')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'INVESTMENT_UPDATE')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'INVESTMENT_DELETE')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'INVESTMENT_CLOSE')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'INVESTMENT_PROFIT_DISTRIBUTE')]").exists());
    }

    @Test
    @DisplayName("Should include analytics and audit permissions")
    @WithMockPermissions(role = "OWNER")
    void shouldIncludeAnalyticsAndAuditPermissions() throws Exception {
        mockMvc.perform(get("/api/permissions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name == 'ANALYTICS_SALES_VIEW')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'ANALYTICS_INVESTMENT_VIEW')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'ANALYTICS_MANAGE')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'AUDIT_LOG_VIEW_SHOP')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'AUDIT_LOG_VIEW_TENANT')]").exists());
    }

    @Test
    @DisplayName("Should include fraud detection permissions")
    @WithMockPermissions(role = "OWNER")
    void shouldIncludeFraudDetectionPermissions() throws Exception {
        mockMvc.perform(get("/api/permissions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name == 'FRAUD_VIEW')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'FRAUD_MANAGE')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'FRAUD_LIST')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'FRAUD_INVESTIGATE')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'FRAUD_RESOLVE')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'FRAUD_DETECT')]").exists());
    }

    @Test
    @DisplayName("Permission response should include id, name, and description")
    @WithMockPermissions(role = "OWNER")
    void permissionResponseShouldIncludeRequiredFields() throws Exception {
        mockMvc.perform(get("/api/permissions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").exists())
            .andExpect(jsonPath("$[0].name").exists())
            .andExpect(jsonPath("$[0].description").exists())
            .andExpect(jsonPath("$[0].id").isNotEmpty())
            .andExpect(jsonPath("$[0].name").isNotEmpty())
            .andExpect(jsonPath("$[0].description").isNotEmpty());
    }
}
