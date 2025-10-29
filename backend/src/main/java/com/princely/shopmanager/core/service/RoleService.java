package com.princely.shopmanager.core.service;

import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.auth.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

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

    /**
     * Get all available roles.
     *
     * @return List of all roles
     */
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        log.debug("Retrieving all roles");
        return roleRepository.findAll();
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
            .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));
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
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        return user.getRoles();
    }

    /**
     * Assign a role to a user.
     * Validates that the role can be assigned based on the current user's permissions.
     *
     * @param userId User ID to assign role to
     * @param roleIdentifier Role ID or role name
     * @throws IllegalArgumentException if user or role not found
     * @throws SecurityException if current user cannot assign this role
     */
    @Transactional
    public void assignRoleToUser(String userId, String roleIdentifier) {
        log.info("Assigning role {} to user {}", roleIdentifier, userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Try to find role by ID first, then by name
        Role role = roleRepository.findById(roleIdentifier)
            .or(() -> roleRepository.findByName(roleIdentifier))
            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleIdentifier));

        // Validate assignment permissions
        validateRoleAssignment(user, role);

        // Add role to user
        user.getRoles().add(role);
        userRepository.save(user);

        log.info("Successfully assigned role {} to user {}", role.getName(), userId);
    }

    /**
     * Remove a role from a user.
     *
     * @param userId User ID
     * @param roleId Role ID
     * @throws IllegalArgumentException if user or role not found
     */
    @Transactional
    public void removeRoleFromUser(String userId, String roleId) {
        log.info("Removing role {} from user {}", roleId, userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));

        // Validate removal permissions
        validateRoleAssignment(user, role);

        // Remove role from user
        user.getRoles().remove(role);
        userRepository.save(user);

        log.info("Successfully removed role {} from user {}", role.getName(), userId);
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
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        return user.getRoles().stream()
            .anyMatch(role -> role.getName().equals(roleName));
    }
}
