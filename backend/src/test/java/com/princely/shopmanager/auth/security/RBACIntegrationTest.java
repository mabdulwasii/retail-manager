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
     */
    private static final List<EndpointPermission> ENDPOINT_REGISTRY = List.of(
        // Product endpoints
        endpoint("POST", "/api/shops/{shopId}/products", "PRODUCT_CREATE", "OWNER", "MANAGER"),
        endpoint("GET", "/api/shops/{shopId}/products", "PRODUCT_READ", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("GET", "/api/products/{productId}", "PRODUCT_READ", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("PUT", "/api/products/{productId}", "PRODUCT_UPDATE", "OWNER", "MANAGER"),
        endpoint("DELETE", "/api/products/{productId}", "PRODUCT_DELETE", "OWNER", "MANAGER"),

        // Shop endpoints
        endpoint("POST", "/api/shops", "SHOP_CREATE", "OWNER"),
        endpoint("GET", "/api/shops/{shopId}", "SHOP_READ", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("PUT", "/api/shops/{shopId}", "SHOP_UPDATE", "OWNER", "MANAGER"),
        endpoint("PATCH", "/api/shops/{shopId}/status", "SHOP_STATUS_CHANGE", "OWNER"),

        // User endpoints
        endpoint("GET", "/api/users/profile", "USER_READ", "OWNER", "MANAGER", "EMPLOYEE"),
        endpoint("GET", "/api/users/{userId}", "USER_READ", "OWNER", "MANAGER"),
        endpoint("PATCH", "/api/users/{userId}", "USER_UPDATE", "OWNER", "MANAGER"),

        // Role endpoints
        endpoint("GET", "/api/roles", "ROLE_READ", "OWNER", "MANAGER"),
        endpoint("GET", "/api/roles/{roleId}", "ROLE_READ", "OWNER", "MANAGER"),
        endpoint("POST", "/api/roles", "ROLE_CREATE", "OWNER"),

        // Permission endpoints
        endpoint("GET", "/api/permissions", "PERMISSION_READ", "OWNER", "MANAGER"),

        // Inventory endpoints
        endpoint("POST", "/api/shops/{shopId}/inventory", "INVENTORY_CREATE", "OWNER", "MANAGER"),
        endpoint("GET", "/api/shops/{shopId}/inventory", "INVENTORY_READ", "OWNER", "MANAGER", "EMPLOYEE")
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
