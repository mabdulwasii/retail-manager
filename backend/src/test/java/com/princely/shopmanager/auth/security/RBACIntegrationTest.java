package com.princely.shopmanager.auth.security;

import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive RBAC Integration Test.
 *
 * This test validates Role-Based Access Control across ALL secured endpoints.
 * It uses a registry pattern to track expected permissions for each endpoint.
 *
 * IMPORTANT: This test MUST FAIL if a new controller endpoint is added without
 * updating the ENDPOINT_REGISTRY. This ensures all endpoints have proper RBAC configured.
 *
 * Roles tested: SYSTEM_ADMIN, TENANT_ADMIN, OWNER, MANAGER, EMPLOYEE, INVESTOR, CASHIER
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "/test-data-empty.sql")
@DisplayName("RBAC - Comprehensive Access Control Integration Test")
class RBACIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TestRestTemplate restTemplate;

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
     * Parameterized test that validates RBAC for each endpoint + role combination.
     * Tests that allowed roles get 200/201 and denied roles get 403.
     */
    @ParameterizedTest(name = "{0} {1} with role {2} should {3}")
    @MethodSource("endpointRolePermutations")
    @DisplayName("Should enforce RBAC correctly for all endpoint-role combinations")
    void shouldEnforceRBACCorrectly(String method, String path, String role, String expectedOutcome, boolean shouldAllow) {
        // Given
        String tenantId = "tenant-rbac-test";
        setTenantContext(tenantId);
        var testData = setupTenantTestData(tenantId);

        // Replace path variables with actual IDs
        String resolvedPath = path
            .replace("{shopId}", testData.get("testShop").toString())
            .replace("{productId}", "dummy-product-id")
            .replace("{userId}", testData.get("testUser").toString())
            .replace("{roleId}", testData.get("testRole").toString());

        // When
        ResponseEntity<String> response = performAuthenticatedRequest(
            method,
            resolvedPath,
            role.toLowerCase(),
            String.class,
            role
        );

        // Then
        if (shouldAllow) {
            assertThat(response.getStatusCode())
                .as("Role %s should have access to %s %s", role, method, path)
                .isIn(HttpStatus.OK, HttpStatus.CREATED, HttpStatus.NO_CONTENT, HttpStatus.NOT_FOUND);
        } else {
            assertThat(response.getStatusCode())
                .as("Role %s should NOT have access to %s %s", role, method, path)
                .isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

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

    private static Stream<Arguments> endpointRolePermutations() {
        List<String> allRoles = List.of("SYSTEM_ADMIN", "TENANT_ADMIN", "OWNER", "MANAGER", "EMPLOYEE", "INVESTOR", "CASHIER");

        return ENDPOINT_REGISTRY.stream()
            .flatMap(endpoint -> allRoles.stream()
                .map(role -> {
                    boolean shouldAllow = role.equals("SYSTEM_ADMIN") || endpoint.allowedRoles.contains(role);
                    String outcome = shouldAllow ? "be allowed" : "be denied";
                    return Arguments.of(endpoint.method, endpoint.path, role, outcome, shouldAllow);
                }));
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
                if (endpoint != null) {
                    endpoints.add(endpoint);
                }
            }
        }

        return endpoints;
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

    private ResponseEntity<String> performAuthenticatedRequest(String method, String path, String username, Class<String> responseType, String role) {
        switch (method) {
            case "GET":
                return performAuthenticatedGet(path, username, responseType, role);
            case "POST":
                return performAuthenticatedPost(path, null, username, responseType, role);
            case "PUT":
                return performAuthenticatedPut(path, null, username, responseType, role);
            case "PATCH":
                return performAuthenticatedPatch(path, null, username, responseType, role);
            case "DELETE":
                ResponseEntity<Void> deleteResponse = performAuthenticatedDelete(path, username, role);
                return ResponseEntity.status(deleteResponse.getStatusCode()).body("");
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
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
