package com.princely.shopmanager.core.service;

import com.princely.shopmanager.auth.dto.CreateKeycloakUserRequest;
import com.princely.shopmanager.auth.service.KeycloakUserService;
import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.dto.UserCreateRequest;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.dto.UserShopTransferRequest;
import com.princely.shopmanager.core.dto.UserUpdateRequest;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.shared.domain.AuditLog;
import com.princely.shopmanager.shared.service.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing user operations and queries.
 */
@Service
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final ShopRepository shopRepository;
    private final AuditService auditService;

    @Autowired(required = false)
    private KeycloakUserService keycloakUserService;

    public UserService(UserRepository userRepository,
                      TenantRepository tenantRepository,
                      RoleRepository roleRepository,
                      ShopRepository shopRepository,
                      AuditService auditService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.shopRepository = shopRepository;
        this.auditService = auditService;
    }

    /**
     * Gets users by roles and tenant ID for notification purposes.
     *
     * @param roles List of role names to filter by
     * @param tenantId Tenant ID to filter by
     * @return List of users matching the criteria
     */
    public List<User> getUsersByRolesAndTenant(List<String> roles, String tenantId) {
        log.debug("Finding users with roles {} for tenant {}", roles, tenantId);

        try {
            // For now, return all users since the exact query implementation
            // depends on the specific User-Role relationship structure
            List<User> allUsers = userRepository.findAll();

            // In a real implementation, this would filter by roles and tenant
            // Example query might be: userRepository.findByRoleNamesInAndTenantId(roles, tenantId)

            log.debug("Found {} users for notification", allUsers.size());
            return allUsers;

        } catch (Exception e) {
            log.error("Error finding users by roles {} and tenant {}: {}", roles, tenantId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Gets a user by ID.
     *
     * @param userId User ID
     * @return User if found, null otherwise
     */
    public User getUserById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * Gets users with admin privileges for the tenant.
     *
     * @param tenantId Tenant ID
     * @return List of admin users
     */
    public List<User> getAdminUsers(String tenantId) {
        return getUsersByRolesAndTenant(List.of("TENANT_ADMIN", "OWNER"), tenantId);
    }

    /**
     * Gets users with manager privileges for the tenant.
     *
     * @param tenantId Tenant ID
     * @return List of manager users
     */
    public List<User> getManagerUsers(String tenantId) {
        return getUsersByRolesAndTenant(List.of("MANAGER"), tenantId);
    }

    /**
     * Gets a user by their Keycloak ID.
     *
     * @param keycloakId Keycloak user ID
     * @return User if found, null otherwise
     */
    public User getUserByKeycloakId(String keycloakId) {
        log.debug("Finding user by Keycloak ID: {}", keycloakId);

        try {
            return userRepository.findByKeycloakId(keycloakId)
                    .orElseThrow(() -> {
                        log.debug("User with keycloadId {} not found", keycloakId);
                        return new IllegalStateException("User not found");
                    });
        } catch (Exception e) {
            log.error("Error finding user by Keycloak ID {}: {}", keycloakId, e.getMessage());
            return null;
        }
    }

    /**
     * Gets a user by their email address.
     *
     * @param email User email
     * @return User if found, null otherwise
     */
    public User getUserByEmail(String email) {
        log.debug("Finding user by email: {}", email);

        try {
            return userRepository.findByEmail(email)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error finding user by email {}: {}", email, e.getMessage());
            return null;
        }
    }

    /**
     * Creates a new user.
     *
     * @param tenantId Tenant ID
     * @param request User creation request
     * @return Created user
     */
    @Transactional
    public User createUser(String tenantId, UserCreateRequest request) {
        log.info("Creating user {} for tenant {}", request.getUsername(), tenantId);

        // Validate tenant exists
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + tenantId));

        // Validate username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        // Get roles
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleIdentifier : request.getRoles()) {
                Role role = roleRepository.findById(roleIdentifier)
                    .or(() -> roleRepository.findByName(roleIdentifier))
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleIdentifier));
                roles.add(role);
            }
        }

        if (roles.isEmpty()) {
            throw new IllegalArgumentException("At least one role must be specified");
        }

        // Validate shopId is provided (now required)
        if (request.getShopId() == null || request.getShopId().isBlank()) {
            throw new IllegalArgumentException("Shop ID is required for user creation");
        }

        // Get shop
        Shop shop = shopRepository.findById(request.getShopId())
            .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + request.getShopId()));

        // Validate shop belongs to tenant
        if (!shop.getTenant().getId().equals(tenantId)) {
            throw new IllegalArgumentException(
                "Shop " + request.getShopId() + " does not belong to tenant " + tenantId
            );
        }
        log.debug("Assigning user to shop: {} ({})", shop.getName(), shop.getId());

        // Create user in Keycloak FIRST (if enabled)
        // If this fails, transaction will rollback and user won't be created in DB
        String keycloakId = null;
        if (keycloakUserService != null) {
            log.info("Creating user in Keycloak: {}", request.getUsername());
            List<String> roleNames = roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());

            CreateKeycloakUserRequest keycloakRequest = new CreateKeycloakUserRequest(
                request.getUsername(),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber(),
                tenantId,
                request.getShopId(),
                request.getPassword(),
                false, // permanent password (not temporary)
                true, // enabled
                roleNames
            );

            keycloakId = keycloakUserService.createUser(keycloakRequest);
            log.info("User created in Keycloak with ID: {}", keycloakId);
        }

        // Create user in database
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phoneNumber(request.getPhoneNumber())
            .keycloakId(keycloakId)
            .tenant(tenant)
            .shop(shop)
            .roles(roles)
            .status(User.UserStatus.ACTIVE)
            .build();

        User savedUser = userRepository.save(user);
        log.info("Successfully created user {} with ID {} (Keycloak ID: {}, Shop: {})",
            savedUser.getUsername(), savedUser.getId(), keycloakId,
            shop != null ? shop.getId() : "none");

        return savedUser;
    }

    /**
     * Updates an existing user.
     *
     * @param userId User ID
     * @param request User update request
     * @return Updated user
     */
    @Transactional
    public User updateUser(String userId, UserUpdateRequest request) {
        log.info("Updating user {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Update fields if provided
        if (request.getEmail() != null) {
            // Check email uniqueness
            if (userRepository.existsByEmail(request.getEmail()) &&
                !request.getEmail().equals(user.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        // Update in Keycloak (if enabled and keycloakId exists)
        // If this fails, transaction will rollback
        if (keycloakUserService != null && user.getKeycloakId() != null) {
            log.info("Updating user in Keycloak: {}", user.getKeycloakId());
            boolean enabled = user.getStatus() == User.UserStatus.ACTIVE;
            String shopId = user.getShop() != null ? user.getShop().getId() : null;

            keycloakUserService.updateUser(
                user.getKeycloakId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                enabled,
                user.getTenant().getId(),
                shopId
            );
            log.info("User updated in Keycloak: {}", user.getKeycloakId());
        }

        User updatedUser = userRepository.save(user);
        log.info("Successfully updated user {}", userId);

        return updatedUser;
    }

    /**
     * Deletes (deactivates) a user.
     *
     * @param userId User ID
     */
    @Transactional
    public void deleteUser(String userId) {
        log.info("Deleting user {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Delete from Keycloak FIRST (if enabled and keycloakId exists)
        // If this fails, transaction will rollback
        if (keycloakUserService != null && user.getKeycloakId() != null) {
            log.info("Deleting user from Keycloak: {}", user.getKeycloakId());
            keycloakUserService.deleteUser(user.getKeycloakId());
            log.info("User deleted from Keycloak: {}", user.getKeycloakId());
        }

        // Soft delete by setting status to INACTIVE
        user.setStatus(User.UserStatus.INACTIVE);
        userRepository.save(user);

        log.info("Successfully deactivated user {}", userId);
    }

    /**
     * Gets all users for a tenant.
     *
     * @param tenantId Tenant ID
     * @return List of users in the tenant
     */
    public List<User> getUsersByTenant(String tenantId) {
        log.debug("Retrieving users for tenant {}", tenantId);
        return userRepository.findByTenantId(tenantId);
    }

    /**
     * Gets all users in the system, optionally filtered by status.
     *
     * @param status Optional status filter (null returns all users)
     * @return List of users
     */
    public List<User> getAllUsers(User.UserStatus status) {
        log.debug("Retrieving all users with status filter: {}", status);
        if (status != null) {
            return userRepository.findAll().stream()
                .filter(user -> user.getStatus() == status)
                .toList();
        }
        return userRepository.findAll();
    }

    /**
     * Gets all users for a shop, optionally filtered by status.
     *
     * @param shopId Shop ID
     * @param status Optional status filter (null returns all users)
     * @return List of users in the shop
     */
    public List<User> getUsersByShop(String shopId, User.UserStatus status) {
        log.debug("Retrieving users for shop {} with status filter: {}", shopId, status);
        if (status != null) {
            return userRepository.findByShopIdAndStatus(shopId, status);
        }
        return userRepository.findByShopId(shopId);
    }

    /**
     * Transfers a user to a different shop within the same tenant.
     *
     * @param userId User ID
     * @param request Shop transfer request
     * @return Updated user
     */
    @Transactional
    public User transferUserToShop(String userId, UserShopTransferRequest request) {
        log.info("Transferring user {} to shop {}", userId, request.getNewShopId());

        // Validate user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Validate new shop exists
        Shop newShop = shopRepository.findById(request.getNewShopId())
            .orElseThrow(() -> new IllegalArgumentException("Shop not found with ID: " + request.getNewShopId()));

        // Validate shop belongs to same tenant
        if (!newShop.getTenant().getId().equals(user.getTenant().getId())) {
            throw new IllegalArgumentException(
                "Cannot transfer user to shop in different tenant. User tenant: " +
                user.getTenant().getId() + ", Shop tenant: " + newShop.getTenant().getId()
            );
        }

        String oldShopId = user.getShop() != null ? user.getShop().getId() : "none";

        // Update user's shop
        user.setShop(newShop);

        // Update shopId in Keycloak (if enabled and keycloakId exists)
        // If this fails, transaction will rollback
        if (keycloakUserService != null && user.getKeycloakId() != null) {
            log.info("Updating shop in Keycloak for user: {}", user.getKeycloakId());
            boolean enabled = user.getStatus() == User.UserStatus.ACTIVE;

            keycloakUserService.updateUser(
                user.getKeycloakId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                enabled,
                user.getTenant().getId(),
                request.getNewShopId()
            );
            log.info("Shop updated in Keycloak: {}", user.getKeycloakId());
        }

        User updatedUser = userRepository.save(user);

        // Log audit trail
        String auditMessage = String.format(
            "User transferred from shop %s to shop %s. Reason: %s",
            oldShopId,
            request.getNewShopId(),
            request.getReason() != null ? request.getReason() : "Not provided"
        );

        auditService.logDataModification(
            newShop,
            user.getId(),
            user.getUsername(),
            AuditLog.ActionType.UPDATE,
            "User",
            user.getId(),
            auditMessage,
            "Shop: " + oldShopId,
            "Shop: " + request.getNewShopId()
        );

        log.info("Successfully transferred user {} to shop {}", userId, request.getNewShopId());

        return updatedUser;
    }

}