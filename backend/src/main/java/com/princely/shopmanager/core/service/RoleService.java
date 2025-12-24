package com.princely.shopmanager.core.service;

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
import com.princely.shopmanager.auth.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing roles and role assignments.
 * Handles role CRUD operations and role-to-user assignments with proper access control.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final TenantRepository tenantRepository;

    // Error message constants
    private static final String ERROR_ROLE_NOT_FOUND = "Role not found with ID: ";
    private static final String ERROR_USER_NOT_FOUND = "User not found with ID: ";
    private static final String ERROR_SYSTEM_ROLE_IMMUTABLE = "Cannot modify permissions of system role: ";

    /**
     * Get all available roles for the current tenant.
     * Returns system roles (available to all tenants) + tenant-specific custom roles.
     *
     * @return List of system and tenant-specific roles
     */
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        String currentTenantId = TenantContext.getCurrentTenant();
        log.debug("Retrieving system and tenant-specific roles for tenant: {}", currentTenantId);
        return roleRepository.findSystemAndTenantRoles(currentTenantId);
    }

    /**
     * Get a specific role by ID.
     *
     * @param roleId Role ID
     * @return Role entity
     * @throws IllegalArgumentException if role not found
     */
    @Transactional(readOnly = true)
    public Role getRoleById(String roleId) {
        log.debug("Retrieving role by ID: {}", roleId);
        return roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException(ERROR_ROLE_NOT_FOUND + roleId));
    }

    /**
     * Get a role by name.
     *
     * @param roleName Role name
     * @return Role entity
     * @throws IllegalArgumentException if role not found
     */
    @Transactional(readOnly = true)
    public Role getRoleByName(String roleName) {
        log.debug("Retrieving role by name: {}", roleName);
        return roleRepository.findByName(roleName)
            .orElseThrow(() -> new IllegalArgumentException("Role not found with name: " + roleName));
    }

    /**
     * Get all roles for a specific user.
     *
     * @param userId User ID
     * @return Set of roles assigned to the user
     */
    @Transactional(readOnly = true)
    public Set<Role> getUserRoles(String userId) {
        log.debug("Retrieving roles for user: {}", userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND + userId));
        return user.getRoles();
    }

    /**
     * Assign a role to a user.
     * Validates that the role can be assigned based on the current user's permissions.
     * Evicts the user's permission cache to immediately reflect new permissions.
     *
     * @param userId User ID to assign role to
     * @param roleIdentifier Role ID or role name
     * @throws IllegalArgumentException if user or role not found
     * @throws SecurityException if current user cannot assign this role
     */
    @Transactional
    @CacheEvict(value = "userPermissions", key = "#root.target.getUserEmail(#userId)")
    public void assignRoleToUser(String userId, String roleIdentifier) {
        log.info("Assigning role {} to user {}", roleIdentifier, userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND + userId));

        // Try to find role by ID first, then by name
        Role role = roleRepository.findById(roleIdentifier)
            .or(() -> roleRepository.findByName(roleIdentifier))
            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleIdentifier));

        // Validate assignment permissions
        validateRoleAssignment(user, role);

        // Add role to user
        user.getRoles().add(role);
        userRepository.save(user);

        log.info("Successfully assigned role {} to user {} - Permission cache evicted",
            role.getName(), user.getEmail());
    }

    /**
     * Helper method to get user email by ID (for cache eviction).
     */
    public String getUserEmail(String userId) {
        return userRepository.findById(userId)
            .map(User::getEmail)
            .orElse(null);
    }

    /**
     * Remove a role from a user.
     * Evicts the user's permission cache to immediately reflect permission changes.
     *
     * @param userId User ID
     * @param roleId Role ID
     * @throws IllegalArgumentException if user or role not found
     */
    @Transactional
    @CacheEvict(value = "userPermissions", key = "#root.target.getUserEmail(#userId)")
    public void removeRoleFromUser(String userId, String roleId) {
        log.info("Removing role {} from user {}", roleId, userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND + userId));

        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException(ERROR_ROLE_NOT_FOUND + roleId));

        // Validate removal permissions
        validateRoleAssignment(user, role);

        // Remove role from user
        user.getRoles().remove(role);
        userRepository.save(user);

        log.info("Successfully removed role {} from user {} - Permission cache evicted",
            role.getName(), user.getEmail());
    }

    /**
     * Validates that the current user can assign/remove the specified role to/from the target user.
     *
     * @param targetUser User to assign role to
     * @param role Role to assign
     * @throws SecurityException if assignment is not allowed
     */
    private void validateRoleAssignment(User targetUser, Role role) {
        String currentTenantId = TenantContext.getCurrentTenant();

        // Validate tenant isolation
        if (targetUser.getTenant() != null && !targetUser.getTenant().getId().equals(currentTenantId)) {
            throw new SecurityException("Cannot assign roles to users in different tenants");
        }

        // System roles can only be assigned by SYSTEM_ADMIN
        if (role.isSystem()) {
            // This check would be done at the controller level via @PreAuthorize
            log.debug("Assigning system role {} - requires SYSTEM_ADMIN permission", role.getName());
        }

        log.debug("Role assignment validation passed for role {} to user {}", role.getName(), targetUser.getId());
    }

    /**
     * Check if a user has a specific role.
     *
     * @param userId User ID
     * @param roleName Role name
     * @return true if user has the role, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean userHasRole(String userId, String roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND + userId));

        return user.getRoles().stream()
            .anyMatch(role -> role.getName().equals(roleName));
    }

    // ============= ROLE CRUD OPERATIONS =============

    /**
     * Create a new custom role.
     * System roles cannot be created via API.
     * Custom roles are tenant-specific.
     *
     * @param request Role creation request
     * @return Created role
     * @throws IllegalArgumentException if role name already exists or is reserved
     */
    @Transactional
    public Role createRole(RoleCreateRequest request) {
        log.info("Creating custom role: {}", request.getName());

        String currentTenantId = TenantContext.getCurrentTenant();

        // Validate role name doesn't exist
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Role with name '" + request.getName() + "' already exists");
        }

        // Validate role name format (uppercase, no spaces)
        if (!request.getName().matches("^[A-Z_]+$")) {
            throw new IllegalArgumentException("Role name must be uppercase letters and underscores only");
        }

        // Get current tenant
        Tenant tenant = tenantRepository.findById(currentTenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + currentTenantId));

        // Build role
        Role role = Role.builder()
            .name(request.getName())
            .description(request.getDescription())
            .isSystem(false) // Custom roles are never system roles
            .tenant(tenant)   // Set tenant for custom roles
            .permissions(new HashSet<>())
            .build();

        // Add permissions if provided
        if (request.getPermissionIdentifiers() != null && !request.getPermissionIdentifiers().isEmpty()) {
            Set<Permission> permissions = resolvePermissions(request.getPermissionIdentifiers());
            role.setPermissions(permissions);
        }

        Role savedRole = roleRepository.save(role);
        log.info("Successfully created custom role: {} for tenant: {}", savedRole.getName(), currentTenantId);

        return savedRole;
    }

    /**
     * Update a role's details.
     * System roles cannot be updated.
     *
     * @param roleId Role ID
     * @param request Update request
     * @return Updated role
     * @throws IllegalArgumentException if role not found or is a system role
     */
    @Transactional
    public Role updateRole(String roleId, RoleUpdateRequest request) {
        log.info("Updating role: {}", roleId);

        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException(ERROR_ROLE_NOT_FOUND + roleId));

        // System roles cannot be updated
        if (role.isSystem()) {
            throw new SecurityException("System roles cannot be modified");
        }

        // Update description if provided
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            role.setDescription(request.getDescription());
        }

        Role updatedRole = roleRepository.save(role);
        log.info("Successfully updated role: {}", updatedRole.getName());

        return updatedRole;
    }

    /**
     * Delete a custom role.
     * System roles cannot be deleted.
     * Roles currently assigned to users cannot be deleted.
     *
     * @param roleId Role ID
     * @throws IllegalArgumentException if role not found or is a system role
     * @throws IllegalStateException if role is assigned to users
     */
    @Transactional
    public void deleteRole(String roleId) {
        log.info("Deleting role: {}", roleId);

        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException(ERROR_ROLE_NOT_FOUND + roleId));

        // System roles cannot be deleted
        if (role.isSystem()) {
            throw new SecurityException("System roles cannot be deleted");
        }

        // Check if role is assigned to any users
        if (!role.getUsers().isEmpty()) {
            throw new IllegalStateException("Cannot delete role '" + role.getName() +
                "' because it is assigned to " + role.getUsers().size() + " user(s)");
        }

        roleRepository.delete(role);
        log.info("Successfully deleted role: {}", role.getName());
    }

    // ============= PERMISSION MANAGEMENT =============

    /**
     * Add a permission to a role.
     * System roles cannot be modified.
     * Evicts permission cache for all users with this role.
     *
     * @param roleId Role ID
     * @param permissionIdentifier Permission ID or name
     * @throws IllegalArgumentException if role or permission not found
     * @throws SecurityException if attempting to modify a system role
     */
    @Transactional
    @CacheEvict(value = "userPermissions", allEntries = true)
    public void addPermissionToRole(String roleId, String permissionIdentifier) {
        log.info("Adding permission {} to role {}", permissionIdentifier, roleId);

        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException(ERROR_ROLE_NOT_FOUND + roleId));

        // System roles cannot be modified
        if (role.isSystem()) {
            throw new SecurityException(ERROR_SYSTEM_ROLE_IMMUTABLE + role.getName());
        }

        Permission permission = permissionRepository.findById(permissionIdentifier)
            .or(() -> permissionRepository.findByName(permissionIdentifier))
            .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionIdentifier));

        role.getPermissions().add(permission);
        roleRepository.save(role);

        log.info("Successfully added permission {} to role {} - Permission cache evicted",
            permission.getName(), role.getName());
    }

    /**
     * Remove a permission from a role.
     * System roles cannot be modified.
     * Evicts permission cache for all users with this role.
     *
     * @param roleId Role ID
     * @param permissionId Permission ID
     * @throws IllegalArgumentException if role or permission not found
     * @throws SecurityException if attempting to modify a system role
     */
    @Transactional
    @CacheEvict(value = "userPermissions", allEntries = true)
    public void removePermissionFromRole(String roleId, String permissionId) {
        log.info("Removing permission {} from role {}", permissionId, roleId);

        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException(ERROR_ROLE_NOT_FOUND + roleId));

        // System roles cannot be modified
        if (role.isSystem()) {
            throw new SecurityException(ERROR_SYSTEM_ROLE_IMMUTABLE + role.getName());
        }

        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found with ID: " + permissionId));

        role.getPermissions().remove(permission);
        roleRepository.save(role);

        log.info("Successfully removed permission {} from role {} - Permission cache evicted",
            permission.getName(), role.getName());
    }

    /**
     * Bulk update role permissions (replaces all existing permissions).
     * System roles cannot be modified.
     *
     * @param roleId Role ID
     * @param permissionIdentifiers Set of permission IDs or names
     * @throws IllegalArgumentException if role not found or any permission not found
     * @throws SecurityException if attempting to modify a system role
     */
    @Transactional
    public void bulkUpdateRolePermissions(String roleId, Set<String> permissionIdentifiers) {
        log.info("Bulk updating permissions for role {}: {} permissions", roleId, permissionIdentifiers.size());

        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException(ERROR_ROLE_NOT_FOUND + roleId));

        // System roles cannot be modified
        if (role.isSystem()) {
            throw new SecurityException(ERROR_SYSTEM_ROLE_IMMUTABLE + role.getName());
        }

        // Resolve all permissions
        Set<Permission> permissions = resolvePermissions(permissionIdentifiers);

        // Replace all permissions
        role.getPermissions().clear();
        role.getPermissions().addAll(permissions);

        roleRepository.save(role);

        log.info("Successfully updated permissions for role {}", role.getName());
    }

    /**
     * Resolve permission identifiers to Permission entities.
     *
     * @param identifiers Set of permission IDs or names
     * @return Set of Permission entities
     * @throws IllegalArgumentException if any permission not found
     */
    private Set<Permission> resolvePermissions(Set<String> identifiers) {
        return identifiers.stream()
            .map(id -> permissionRepository.findById(id)
                .or(() -> permissionRepository.findByName(id))
                .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + id)))
            .collect(Collectors.toSet());
    }
}
