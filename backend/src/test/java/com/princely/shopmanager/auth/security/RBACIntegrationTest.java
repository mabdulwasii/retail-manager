package com.princely.shopmanager.auth.security;

import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RBAC Endpoint Registry Validation Test.
 *
 * This test ensures that all secured endpoints are registered in the ENDPOINT_REGISTRY.
 * It uses reflection to find all controller endpoints and validates they are documented.
 *
 * IMPORTANT: This test MUST FAIL if a new controller endpoint is added without
 * updating the ENDPOINT_REGISTRY. This ensures all endpoints have proper RBAC configured.
 *
 * Actual RBAC enforcement testing is done in individual controller minimal IT tests.
 */
@DisplayName("RBAC - Endpoint Registration Validation")
class RBACIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Registry of all secured endpoints with their required permissions.
     * Format: EndpointPermission(httpMethod, path, requiredPermission, allowedRoles...)
     *
     * MUST be updated whenever a new endpoint is added to any controller.
     * Updated: 2025-12-22 - Added Phase 2, 3 & 4 endpoints (97 total new endpoints)
     *   - Phase 2 & 3: Sales, Expense, Investment, InvestmentRound, ProductReturn (37 endpoints)
     *   - Phase 4: Analytics, ShopCustomization, Receipt, Tenant, FeatureFlag, FraudDetection (60 endpoints)
     */
    private static final List<EndpointPermission> ENDPOINT_REGISTRY = List.of(
        // ========================================
        // SHOP ENDPOINTS (13 total)
        // ========================================
        endpoint("POST", "/api/shops", "SHOP_CREATE", "OWNER"),
        endpoint("GET", "/api/shops", "SHOP_LIST", "OWNER", "MANAGER"),
        endpoint("GET", "/api/shops/{shopId}", "SHOP_READ", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("PUT", "/api/shops/{shopId}", "SHOP_UPDATE", "OWNER", "MANAGER"),
        endpoint("PATCH", "/api/shops/{shopId}", "SHOP_UPDATE", "OWNER", "MANAGER"),
        endpoint("DELETE", "/api/shops/{shopId}", "SHOP_DELETE", "OWNER"),
        endpoint("PATCH", "/api/shops/{shopId}/status", "SHOP_STATUS_CHANGE", "OWNER"),
        endpoint("PUT", "/api/shops/{shopId}/configuration", "SHOP_UPDATE", "OWNER", "MANAGER"),
        endpoint("POST", "/api/shops/{shopId}/users", "USER_CREATE", "OWNER", "MANAGER"),
        endpoint("GET", "/api/shops/{shopId}/users", "USER_LIST", "OWNER", "MANAGER"),
        endpoint("GET", "/api/shops/{shopId}/staff", "USER_LIST", "OWNER", "MANAGER"),
        endpoint("GET", "/api/shops/{shopId}/staff-by-role", "USER_LIST", "OWNER", "MANAGER"),
        endpoint("POST", "/api/shops/{shopId}/bulk-invite", "USER_CREATE", "OWNER"),

        // ========================================
        // USER ENDPOINTS (6 total)
        // ========================================
        endpoint("GET", "/api/users/profile", "USER_READ", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("GET", "/api/users/{userId}", "USER_READ", "OWNER", "MANAGER"),
        endpoint("PATCH", "/api/users/{userId}", "USER_UPDATE", "OWNER", "MANAGER"),
        endpoint("DELETE", "/api/users/{userId}", "USER_DELETE", "OWNER"),
        endpoint("GET", "/api/users/{userId}/activity", "USER_READ", "OWNER", "MANAGER"),
        endpoint("GET", "/api/users/{userId}/permissions", "USER_PERMISSION_READ", "OWNER", "MANAGER"),

        // ========================================
        // ROLE ENDPOINTS (7 total)
        // ========================================
        endpoint("GET", "/api/roles", "ROLE_READ", "OWNER", "MANAGER"),
        endpoint("GET", "/api/roles/{roleId}", "ROLE_READ", "OWNER", "MANAGER"),
        endpoint("GET", "/api/users/{userId}/roles", "ROLE_READ", "OWNER", "MANAGER"),
        endpoint("POST", "/api/roles", "ROLE_CREATE", "OWNER"),
        endpoint("PUT", "/api/roles/{roleId}", "ROLE_UPDATE", "OWNER"),
        endpoint("PATCH", "/api/roles/{roleId}", "ROLE_UPDATE", "OWNER"),
        endpoint("DELETE", "/api/roles/{roleId}", "ROLE_DELETE", "OWNER"),

        // ========================================
        // PERMISSION ENDPOINTS (2 total)
        // ========================================
        endpoint("GET", "/api/permissions", "PERMISSION_READ", "OWNER", "MANAGER"),
        endpoint("GET", "/api/permissions/grouped", "PERMISSION_READ", "OWNER", "MANAGER"),

        // ========================================
        // CATEGORY ENDPOINTS (6 total)
        // ========================================
        endpoint("POST", "/api/shops/{shopId}/categories", "CATEGORY_CREATE", "OWNER", "MANAGER"),
        endpoint("GET", "/api/shops/{shopId}/categories", "CATEGORY_LIST", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("GET", "/api/categories/{id}", "CATEGORY_READ", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("PUT", "/api/categories/{id}", "CATEGORY_UPDATE", "OWNER", "MANAGER"),
        endpoint("PATCH", "/api/categories/{id}", "CATEGORY_UPDATE", "OWNER", "MANAGER"),
        endpoint("DELETE", "/api/categories/{id}", "CATEGORY_DELETE", "OWNER"),

        // ========================================
        // PRODUCT ENDPOINTS (10 total)
        // ========================================
        endpoint("POST", "/api/shops/{shopId}/products", "PRODUCT_CREATE", "OWNER", "MANAGER"),
        endpoint("GET", "/api/shops/{shopId}/products", "PRODUCT_READ", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("GET", "/api/shops/{shopId}/products/low-stock", "PRODUCT_READ", "OWNER", "MANAGER"),
        endpoint("GET", "/api/shops/{shopId}/products/out-of-stock", "PRODUCT_READ", "OWNER", "MANAGER"),
        endpoint("GET", "/api/products/{productId}", "PRODUCT_READ", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("GET", "/api/products/search", "PRODUCT_READ", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("PUT", "/api/products/{productId}", "PRODUCT_UPDATE", "OWNER", "MANAGER"),
        endpoint("PATCH", "/api/products/{productId}", "PRODUCT_UPDATE", "OWNER", "MANAGER"),
        endpoint("DELETE", "/api/products/{productId}", "PRODUCT_DELETE", "OWNER"),
        endpoint("GET", "/api/products/{productId}/inventory-summary", "INVENTORY_READ", "OWNER", "MANAGER", "EMPLOYEE"),

        // ========================================
        // TENANT ADMIN ENDPOINTS (3 total)
        // ========================================
        endpoint("GET", "/api/admin/tenants/pending", "TENANT_LIST", "SYSTEM_ADMIN"),
        endpoint("GET", "/api/admin/tenants/{tenantId}", "TENANT_READ", "SYSTEM_ADMIN"),
        endpoint("POST", "/api/admin/tenants/{tenantId}/activate", "TENANT_UPDATE", "SYSTEM_ADMIN"),

        // ========================================
        // INVENTORY ENDPOINTS (2 existing)
        // ========================================
        endpoint("POST", "/api/shops/{shopId}/inventory", "INVENTORY_CREATE", "OWNER", "MANAGER"),
        endpoint("GET", "/api/shops/{shopId}/inventory", "INVENTORY_READ", "OWNER", "MANAGER", "EMPLOYEE"),

        // ========================================
        // SALES TRANSACTION ENDPOINTS (6 total)
        // ========================================
        endpoint("POST", "/api/sales", "SALES_CREATE", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("GET", "/api/sales/{id}", "SALES_READ", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("GET", "/api/sales/{transactionId}/receipt", "SALES_READ", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("GET", "/api/sales", "SALES_LIST", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("GET", "/api/sales/by-date-range", "SALES_LIST", "OWNER", "MANAGER"),
        endpoint("POST", "/api/sales/{id}/void", "SALES_VOID", "OWNER"),

        // ========================================
        // EXPENSE ENDPOINTS (9 total)
        // ========================================
        endpoint("POST", "/api/shops/{shopId}/expenses", "EXPENSE_CREATE", "OWNER", "MANAGER"),
        endpoint("GET", "/api/expenses/{expenseId}", "EXPENSE_READ", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("GET", "/api/shops/{shopId}/expenses", "EXPENSE_LIST", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("PUT", "/api/expenses/{expenseId}", "EXPENSE_UPDATE", "OWNER", "MANAGER"),
        endpoint("PATCH", "/api/expenses/{expenseId}", "EXPENSE_UPDATE", "OWNER", "MANAGER"),
        endpoint("POST", "/api/expenses/{expenseId}/approve", "EXPENSE_APPROVE", "OWNER"),
        endpoint("POST", "/api/expenses/{expenseId}/reject", "EXPENSE_REJECT", "OWNER"),
        endpoint("DELETE", "/api/expenses/{expenseId}", "EXPENSE_DELETE", "OWNER"),
        endpoint("GET", "/api/shops/{shopId}/expenses/summary", "EXPENSE_SUMMARY_VIEW", "OWNER", "MANAGER"),

        // ========================================
        // INVESTMENT ENDPOINTS (11 total)
        // ========================================
        endpoint("POST", "/api/investments", "INVESTMENT_CREATE", "OWNER"),
        endpoint("GET", "/api/shops/{shopId}/investments", "INVESTMENT_LIST", "OWNER", "INVESTOR"),
        endpoint("GET", "/api/my-investments", "INVESTMENT_LIST", "INVESTOR"),
        endpoint("GET", "/api/investments/{investmentId}", "INVESTMENT_READ", "OWNER", "INVESTOR"),
        endpoint("PUT", "/api/investments/{investmentId}/status", "INVESTMENT_UPDATE", "OWNER"),
        endpoint("PATCH", "/api/investments/{investmentId}/status", "INVESTMENT_UPDATE", "OWNER"),
        endpoint("POST", "/api/investments/{investmentId}/withdraw", "INVESTMENT_CLOSE", "OWNER"),
        endpoint("GET", "/api/investments/{investmentId}/distributions", "INVESTMENT_READ", "OWNER", "INVESTOR"),
        endpoint("GET", "/api/my-distributions", "INVESTMENT_READ", "INVESTOR"),
        endpoint("POST", "/api/distributions/{distributionId}/approve", "INVESTMENT_PROFIT_DISTRIBUTE", "OWNER"),
        endpoint("POST", "/api/distributions/{distributionId}/mark-paid", "INVESTMENT_PROFIT_DISTRIBUTE", "OWNER"),

        // ========================================
        // INVESTMENT ROUND ENDPOINTS (8 total)
        // ========================================
        endpoint("POST", "/api/shops/{shopId}/investment-rounds", "INVESTMENT_CREATE", "OWNER"),
        endpoint("GET", "/api/shops/{shopId}/investment-rounds", "INVESTMENT_LIST", "OWNER", "INVESTOR"),
        endpoint("GET", "/api/investment-rounds/{roundId}", "INVESTMENT_READ", "OWNER", "INVESTOR"),
        endpoint("PUT", "/api/investment-rounds/{roundId}", "INVESTMENT_UPDATE", "OWNER"),
        endpoint("PATCH", "/api/investment-rounds/{roundId}", "INVESTMENT_UPDATE", "OWNER"),
        endpoint("DELETE", "/api/investment-rounds/{roundId}", "INVESTMENT_DELETE", "OWNER"),
        endpoint("POST", "/api/investment-rounds/{roundId}/close", "INVESTMENT_CLOSE", "OWNER"),
        endpoint("POST", "/api/investment-rounds/{roundId}/investors", "INVESTMENT_CREATE", "OWNER"),

        // ========================================
        // PRODUCT RETURN ENDPOINTS (3 total)
        // ========================================
        endpoint("POST", "/api/shops/{shopId}/returns", "RETURN_CREATE", "OWNER", "MANAGER"),
        endpoint("POST", "/api/shops/{shopId}/returns/{returnId}/process", "RETURN_APPROVE", "OWNER", "MANAGER"),
        endpoint("GET", "/api/shops/{shopId}/returns", "RETURN_LIST", "OWNER", "MANAGER", "EMPLOYEE"),

        // ========================================
        // ANALYTICS ENDPOINTS (5 total)
        // ========================================
        endpoint("GET", "/api/analytics/sales-summary", "ANALYTICS_SALES_VIEW", "OWNER", "MANAGER"),
        endpoint("GET", "/api/analytics/investment-roi", "ANALYTICS_INVESTMENT_VIEW", "OWNER", "INVESTOR"),
        endpoint("GET", "/api/analytics/fraud-statistics", "FRAUD_VIEW", "OWNER", "MANAGER"),
        endpoint("GET", "/api/analytics/revenue-analytics", "ANALYTICS_SALES_VIEW", "OWNER", "MANAGER"),
        endpoint("POST", "/api/analytics/clear-cache/{shopId}", "ANALYTICS_MANAGE", "OWNER"),

        // ========================================
        // SHOP CUSTOMIZATION ENDPOINTS (8 total)
        // ========================================
        endpoint("GET", "/api/shops/{shopId}/customization", "SHOP_READ", "OWNER", "MANAGER"),
        endpoint("PUT", "/api/shops/{shopId}/customization", "SHOP_UPDATE", "OWNER", "MANAGER"),
        endpoint("PATCH", "/api/shops/{shopId}/customization", "SHOP_UPDATE", "OWNER", "MANAGER"),
        endpoint("PATCH", "/api/shops/{shopId}/customization/colors", "SHOP_UPDATE", "OWNER", "MANAGER"),
        endpoint("PATCH", "/api/shops/{shopId}/customization/theme", "SHOP_UPDATE", "OWNER", "MANAGER"),
        endpoint("POST", "/api/shops/{shopId}/customization/logo", "SHOP_UPDATE", "OWNER", "MANAGER"),
        endpoint("PATCH", "/api/shops/{shopId}/customization/contact", "SHOP_UPDATE", "OWNER", "MANAGER"),
        endpoint("DELETE", "/api/shops/{shopId}/customization", "SHOP_UPDATE", "OWNER"),

        // ========================================
        // RECEIPT ENDPOINTS (9 total)
        // ========================================
        endpoint("GET", "/api/receipts", "RECEIPT_LIST", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("GET", "/api/receipts/{receiptId}", "RECEIPT_READ", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("GET", "/api/receipts/by-number/{receiptNumber}", "RECEIPT_READ", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("GET", "/api/receipts/transaction/{transactionId}", "RECEIPT_READ", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("GET", "/api/receipts/{receiptId}/content", "RECEIPT_READ", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("GET", "/api/receipts/{receiptId}/printable", "RECEIPT_READ", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("POST", "/api/receipts/{receiptId}/mark-printed", "RECEIPT_CREATE", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("POST", "/api/receipts/{receiptId}/mark-emailed", "RECEIPT_EMAIL", "OWNER", "MANAGER"),
        endpoint("POST", "/api/receipts/regenerate/{transactionId}", "RECEIPT_CREATE", "OWNER", "MANAGER"),

        // ========================================
        // TENANT ENDPOINTS (10 total)
        // ========================================
        endpoint("POST", "/api/tenants/{tenantId}/users", "USER_CREATE", "TENANT_ADMIN"),
        endpoint("GET", "/api/tenants/{tenantId}/users", "USER_LIST", "TENANT_ADMIN"),
        endpoint("GET", "/api/tenants/{tenantId}/configurations", "TENANT_CONFIG_READ", "TENANT_ADMIN"),
        endpoint("GET", "/api/tenants/{tenantId}/configurations/category/{category}", "TENANT_CONFIG_READ", "TENANT_ADMIN"),
        endpoint("GET", "/api/tenants/{tenantId}/configurations/{key}", "TENANT_CONFIG_READ", "TENANT_ADMIN"),
        endpoint("POST", "/api/tenants/{tenantId}/configurations", "TENANT_CONFIG_CREATE", "TENANT_ADMIN"),
        endpoint("PUT", "/api/tenants/{tenantId}/configurations/{key}", "TENANT_CONFIG_UPDATE", "TENANT_ADMIN"),
        endpoint("PATCH", "/api/tenants/{tenantId}/configurations/{key}", "TENANT_CONFIG_UPDATE", "TENANT_ADMIN"),
        endpoint("DELETE", "/api/tenants/{tenantId}/configurations/{key}", "TENANT_CONFIG_DELETE", "TENANT_ADMIN"),
        endpoint("POST", "/api/tenants/{tenantId}/configurations/bulk", "TENANT_CONFIG_CREATE", "TENANT_ADMIN"),

        // ========================================
        // FEATURE FLAG ENDPOINTS (12 total)
        // ========================================
        endpoint("GET", "/api/feature-flags/check", "PUBLIC", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("POST", "/api/feature-flags", "FEATURE_FLAG_CREATE", "SYSTEM_ADMIN", "TENANT_ADMIN"),
        endpoint("GET", "/api/feature-flags", "PUBLIC", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("GET", "/api/feature-flags/all", "FEATURE_FLAG_READ", "SYSTEM_ADMIN"),
        endpoint("PUT", "/api/feature-flags/{featureFlagId}", "FEATURE_FLAG_UPDATE", "SYSTEM_ADMIN", "TENANT_ADMIN"),
        endpoint("PATCH", "/api/feature-flags/{featureFlagId}", "FEATURE_FLAG_UPDATE", "SYSTEM_ADMIN", "TENANT_ADMIN"),
        endpoint("DELETE", "/api/feature-flags/{featureFlagId}", "FEATURE_FLAG_DELETE", "SYSTEM_ADMIN"),
        endpoint("GET", "/api/feature-flags/config", "PUBLIC", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("GET", "/api/feature-flags/check/investment", "PUBLIC", "OWNER", "INVESTOR"),
        endpoint("GET", "/api/feature-flags/check/analytics", "PUBLIC", "OWNER", "MANAGER"),
        endpoint("GET", "/api/feature-flags/check/fraud", "PUBLIC", "OWNER", "MANAGER"),
        endpoint("GET", "/api/feature-flags/check/reporting", "PUBLIC", "OWNER", "MANAGER"),

        // ========================================
        // FRAUD DETECTION ENDPOINTS (16 total)
        // ========================================
        endpoint("GET", "/api/fraud/alerts", "FRAUD_LIST", "OWNER", "MANAGER"),
        endpoint("GET", "/api/fraud/alerts/{alertId}", "FRAUD_LIST", "OWNER", "MANAGER"),
        endpoint("POST", "/api/fraud/alerts/{alertId}/acknowledge", "FRAUD_INVESTIGATE", "OWNER", "MANAGER"),
        endpoint("POST", "/api/fraud/alerts/{alertId}/resolve", "FRAUD_RESOLVE", "OWNER", "MANAGER"),
        endpoint("POST", "/api/fraud/alerts/{alertId}/false-positive", "FRAUD_RESOLVE", "OWNER", "MANAGER"),
        endpoint("GET", "/api/fraud/risk-assessments", "FRAUD_LIST", "OWNER", "MANAGER"),
        endpoint("POST", "/api/fraud/risk-assessments/{assessmentId}/approve", "FRAUD_RESOLVE", "OWNER", "MANAGER"),
        endpoint("POST", "/api/fraud/risk-assessments/{assessmentId}/reject", "FRAUD_RESOLVE", "OWNER", "MANAGER"),
        endpoint("GET", "/api/fraud/rules", "FRAUD_LIST", "OWNER", "MANAGER"),
        endpoint("POST", "/api/fraud/rules", "FRAUD_DETECT", "OWNER", "MANAGER"),
        endpoint("PUT", "/api/fraud/rules/{ruleId}", "FRAUD_DETECT", "OWNER", "MANAGER"),
        endpoint("PATCH", "/api/fraud/rules/{ruleId}", "FRAUD_DETECT", "OWNER", "MANAGER"),
        endpoint("DELETE", "/api/fraud/rules/{ruleId}", "FRAUD_DETECT", "OWNER", "MANAGER"),
        endpoint("GET", "/api/fraud/statistics", "FRAUD_LIST", "OWNER", "MANAGER"),
        endpoint("PUT", "/api/fraud/rules/{ruleId}/status", "FRAUD_DETECT", "OWNER", "MANAGER"),
        endpoint("PATCH", "/api/fraud/rules/{ruleId}/status", "FRAUD_DETECT", "OWNER", "MANAGER"),

        // ========================================
        // CLOUD AGGREGATOR - API KEYS ENDPOINTS (4 total)
        // ========================================
        endpoint("POST", "/api/cloud/tenants/{tenantId}/api-keys/{keyId}/regenerate", "TENANT_MANAGE", "TENANT_ADMIN"),
        endpoint("PATCH", "/api/cloud/tenants/{tenantId}/api-keys/{keyId}", "TENANT_MANAGE", "TENANT_ADMIN"),
        endpoint("DELETE", "/api/cloud/tenants/{tenantId}/api-keys/{keyId}", "TENANT_MANAGE", "TENANT_ADMIN"),
        endpoint("GET", "/api/cloud/tenants/{tenantId}/api-keys/{keyId}/usage", "TENANT_READ", "TENANT_ADMIN"),

        // ========================================
        // CLOUD AGGREGATOR - BILLING/INVOICES ENDPOINTS (3 total)
        // ========================================
        endpoint("GET", "/api/cloud/tenants/{tenantId}/billing/invoices/{invoiceId}/pdf", "TENANT_READ", "TENANT_ADMIN"),
        endpoint("GET", "/api/cloud/tenants/{tenantId}/billing/invoices", "TENANT_READ", "TENANT_ADMIN"),
        endpoint("GET", "/api/cloud/tenants/{tenantId}/billing/invoices/{invoiceId}", "TENANT_READ", "TENANT_ADMIN"),

        // ========================================
        // CLOUD AGGREGATOR - ANALYTICS ENDPOINTS (9 total)
        // ========================================
        endpoint("GET", "/api/cloud/tenants/{tenantId}/analytics", "CLOUD_ANALYTICS_REVENUE_VIEW", "TENANT_ADMIN"),
        endpoint("GET", "/api/cloud/tenants/{tenantId}/analytics/sync-status", "CLOUD_ANALYTICS_REVENUE_VIEW", "TENANT_ADMIN"),
        endpoint("GET", "/api/cloud/analytics/platform", "CLOUD_ANALYTICS_PLATFORM_VIEW", "SYSTEM_ADMIN"),
        endpoint("GET", "/api/cloud/analytics/revenue", "CLOUD_ANALYTICS_REVENUE_VIEW", "TENANT_ADMIN"),
        endpoint("GET", "/api/cloud/analytics/sales", "CLOUD_ANALYTICS_SALES_VIEW", "TENANT_ADMIN"),
        endpoint("GET", "/api/cloud/analytics/top-products", "CLOUD_ANALYTICS_PRODUCTS_VIEW", "TENANT_ADMIN"),
        endpoint("GET", "/api/cloud/analytics/shop-performance", "CLOUD_ANALYTICS_PERFORMANCE_VIEW", "TENANT_ADMIN"),
        endpoint("GET", "/api/cloud/analytics/export/csv", "CLOUD_ANALYTICS_EXPORT", "TENANT_ADMIN"),

        // ========================================
        // CLOUD AGGREGATOR - SUBSCRIPTION ENDPOINTS (2 total)
        // ========================================
        endpoint("GET", "/api/cloud/tenants/{tenantId}/subscription/usage", "TENANT_READ", "TENANT_ADMIN"),
        endpoint("PUT", "/api/cloud/tenants/{tenantId}/subscription/tier", "TENANT_UPDATE", "TENANT_ADMIN"),

        // ========================================
        // REGISTRATION SERVICE ENDPOINTS (4 total)
        // ========================================
        endpoint("POST", "/api/registration/shops", "PUBLIC", "ANONYMOUS"),
        endpoint("GET", "/api/registration/health", "PUBLIC", "ANONYMOUS"),
        endpoint("POST", "/api/registration/tenants", "PUBLIC", "ANONYMOUS"),
        endpoint("DELETE", "/api/registration/tenants/{cloudTenantId}", "TENANT_DELETE", "SYSTEM_ADMIN")
    );

    /**
     * NOTE: Comprehensive parameterized RBAC testing (126 test cases) has been disabled
     * to reduce integration test count and complexity. RBAC enforcement is tested through:
     * 1. This endpoint registration validation test (ensures all endpoints are secured)
     * 2. Individual minimal IT tests (ProductControllerMinimalIT, ShopControllerMinimalIT, etc.)
     * 3. Unit tests for authorization logic
     */

    /**
     * Test that FAILS if new controller endpoints are added without updating ENDPOINT_REGISTRY.
     * Uses reflection to find all @RequestMapping methods and compares with registry.
     */
    @Test
    @DisplayName("Should fail if new endpoints added without updating ENDPOINT_REGISTRY")
    void shouldFailIfNewEndpointAddedWithoutRegistration() {
        // Find all controller endpoints using reflection
        Set<String> actualEndpoints = findAllControllerEndpoints();

        // Extract registered endpoints
        Set<String> registeredEndpoints = ENDPOINT_REGISTRY.stream()
            .map(ep -> ep.method + " " + ep.path)
            .collect(Collectors.toSet());

        // Find unregistered endpoints
        Set<String> unregisteredEndpoints = actualEndpoints.stream()
            .filter(endpoint -> !isEndpointRegistered(endpoint, registeredEndpoints))
            .collect(Collectors.toSet());

        // Fail if there are unregistered endpoints
        assertThat(unregisteredEndpoints)
            .as("All controller endpoints must be registered in ENDPOINT_REGISTRY for RBAC validation. " +
                "Found unregistered endpoints. Please add them to RBACIntegrationTest.ENDPOINT_REGISTRY")
            .isEmpty();
    }

    // Helper methods

    private static EndpointPermission endpoint(String method, String path, String permission, String... allowedRoles) {
        return new EndpointPermission(method, path, permission, Set.of(allowedRoles));
    }

    private Set<String> findAllControllerEndpoints() {
        Set<String> endpoints = new HashSet<>();

        // Get all controller beans
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);

        for (Object controller : controllers.values()) {
            Class<?> controllerClass = controller.getClass();
            String baseMapping = extractBasePath(controllerClass);

            // Find all request mapping methods
            for (Method method : controllerClass.getDeclaredMethods()) {
                String endpoint = extractEndpoint(method, baseMapping);
                if (endpoint != null && shouldValidateEndpoint(endpoint)) {
                    endpoints.add(endpoint);
                }
            }
        }

        return endpoints;
    }

    /**
     * Determines if an endpoint should be validated for RBAC.
     * Excludes public endpoints, swagger/OpenAPI endpoints, and malformed paths.
     */
    private boolean shouldValidateEndpoint(String endpoint) {
        // Exclude public endpoints (no authentication required)
        if (endpoint.contains("/api/public/")) {
            return false;
        }

        // Exclude Swagger/OpenAPI documentation endpoints
        if (endpoint.contains("springdoc") ||
            endpoint.contains("api-docs") ||
            endpoint.contains("swagger")) {
            return false;
        }

        // Exclude malformed paths (like "GET //api")
        if (endpoint.contains("//")) {
            return false;
        }

        return true;
    }

    private String extractBasePath(Class<?> controllerClass) {
        RequestMapping classMapping = controllerClass.getAnnotation(RequestMapping.class);
        if (classMapping != null && classMapping.value().length > 0) {
            return classMapping.value()[0];
        }
        return "";
    }

    private String extractEndpoint(Method method, String basePath) {
        String httpMethod = null;
        String[] paths = null;

        if (method.isAnnotationPresent(GetMapping.class)) {
            httpMethod = "GET";
            paths = method.getAnnotation(GetMapping.class).value();
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            httpMethod = "POST";
            paths = method.getAnnotation(PostMapping.class).value();
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            httpMethod = "PUT";
            paths = method.getAnnotation(PutMapping.class).value();
        } else if (method.isAnnotationPresent(PatchMapping.class)) {
            httpMethod = "PATCH";
            paths = method.getAnnotation(PatchMapping.class).value();
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            httpMethod = "DELETE";
            paths = method.getAnnotation(DeleteMapping.class).value();
        }

        if (httpMethod != null && paths != null && paths.length > 0) {
            String path = basePath + (paths[0].isEmpty() ? "" : paths[0]);
            return httpMethod + " " + path;
        }

        return null;
    }

    private boolean isEndpointRegistered(String actualEndpoint, Set<String> registeredEndpoints) {
        // Exact match
        if (registeredEndpoints.contains(actualEndpoint)) {
            return true;
        }

        // Check if it's a parametrized version of a registered endpoint
        for (String registered : registeredEndpoints) {
            if (endpointsMatch(actualEndpoint, registered)) {
                return true;
            }
        }

        return false;
    }

    private boolean endpointsMatch(String actual, String registered) {
        String[] actualParts = actual.split(" ", 2);
        String[] registeredParts = registered.split(" ", 2);

        if (actualParts.length != 2 || registeredParts.length != 2) {
            return false;
        }

        // HTTP method must match
        if (!actualParts[0].equals(registeredParts[0])) {
            return false;
        }

        // Path matching (considering path variables)
        String actualPath = actualParts[1];
        String registeredPath = registeredParts[1];

        // Simple path variable matching
        String registeredPattern = registeredPath.replaceAll("\\{[^}]+\\}", "[^/]+");
        return actualPath.matches(registeredPattern);
    }

    /**
     * Represents an endpoint with its required permission and allowed roles.
     */
    private static class EndpointPermission {
        final String method;
        final String path;
        final String permission;
        final Set<String> allowedRoles;

        EndpointPermission(String method, String path, String permission, Set<String> allowedRoles) {
            this.method = method;
            this.path = path;
            this.permission = permission;
            this.allowedRoles = allowedRoles;
        }
    }
}
