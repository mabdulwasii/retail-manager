package com.princely.shopmanager.core.service;

import com.princely.shopmanager.core.domain.Permission;
import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.dto.PermissionGroupResponse;
import com.princely.shopmanager.core.dto.PermissionResponse;
import com.princely.shopmanager.core.repository.PermissionRepository;
import com.princely.shopmanager.core.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing permissions.
 * Handles permission listing, grouping, and role-permission queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    /**
     * Get all permissions in the system.
     *
     * @return List of all permissions
     */
    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        log.debug("Retrieving all permissions");
        return permissionRepository.findAll();
    }

    /**
     * Get all permissions grouped by resource.
     *
     * @return Map of resource name to list of permissions
     */
    @Transactional(readOnly = true)
    public Map<String, List<PermissionResponse>> getPermissionsGroupedByResource() {
        log.debug("Retrieving permissions grouped by resource");

        List<Permission> allPermissions = permissionRepository.findAll();

        return allPermissions.stream()
            .collect(Collectors.groupingBy(
                Permission::getResource,
                Collectors.mapping(
                    PermissionResponse::fromEntity,
                    Collectors.toList()
                )
            ));
    }

    /**
     * Get permissions grouped by resource as response DTOs.
     *
     * @return List of PermissionGroupResponse
     */
    @Transactional(readOnly = true)
    public List<PermissionGroupResponse> getPermissionGroupsAsResponse() {
        Map<String, List<PermissionResponse>> grouped = getPermissionsGroupedByResource();

        return grouped.entrySet().stream()
            .map(entry -> PermissionGroupResponse.builder()
                .resource(entry.getKey())
                .permissions(entry.getValue())
                .count(entry.getValue().size())
                .build())
            .sorted((a, b) -> a.getResource().compareTo(b.getResource()))
            .collect(Collectors.toList());
    }

    /**
     * Get all permissions for a specific role.
     *
     * @param roleId Role ID
     * @return Set of permissions assigned to the role
     */
    @Transactional(readOnly = true)
    public Set<Permission> getPermissionsByRole(String roleId) {
        log.debug("Retrieving permissions for role: {}", roleId);

        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));

        return role.getPermissions();
    }

    /**
     * Get a permission by its name.
     *
     * @param permissionName Permission name (e.g., "PRODUCT_CREATE")
     * @return Permission entity
     */
    @Transactional(readOnly = true)
    public Permission getPermissionByName(String permissionName) {
        log.debug("Retrieving permission by name: {}", permissionName);

        return permissionRepository.findByName(permissionName)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found with name: " + permissionName));
    }

    /**
     * Get a permission by its ID or name.
     *
     * @param identifier Permission ID or name
     * @return Permission entity
     */
    @Transactional(readOnly = true)
    public Permission getPermissionByIdentifier(String identifier) {
        log.debug("Retrieving permission by identifier: {}", identifier);

        return permissionRepository.findById(identifier)
            .or(() -> permissionRepository.findByName(identifier))
            .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + identifier));
    }

    /**
     * Get all permissions for a specific resource.
     *
     * @param resource Resource name (e.g., "PRODUCT", "SALES")
     * @return List of permissions for the resource
     */
    @Transactional(readOnly = true)
    public List<Permission> getPermissionsByResource(String resource) {
        log.debug("Retrieving permissions for resource: {}", resource);
        return permissionRepository.findByResource(resource.toUpperCase());
    }

    /**
     * Check if a permission exists by name.
     *
     * @param permissionName Permission name
     * @return true if permission exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean permissionExists(String permissionName) {
        return permissionRepository.findByName(permissionName).isPresent();
    }

    /**
     * Get distinct list of all resources.
     *
     * @return List of resource names
     */
    @Transactional(readOnly = true)
    public List<String> getAllResources() {
        log.debug("Retrieving all distinct resources");

        return permissionRepository.findAll().stream()
            .map(Permission::getResource)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
}
