package com.princely.shopmanager.core.service;

import com.princely.shopmanager.auth.dto.CreateKeycloakUserRequest;
import com.princely.shopmanager.auth.service.KeycloakUserService;
import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.shared.service.AuditService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.princely.shopmanager.auth.constants.SecurityRoles.ROLE_SYSTEM_ADMIN;

/**
 * Service responsible for bootstrapping super admin user on application startup
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SuperAdminBootstrapService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final KeycloakUserService keycloakUserService;
    private final AuditService auditService;

    @Value("${app.bootstrap.superadmin.enabled:true}")
    private boolean bootstrapEnabled;

    @Value("${app.bootstrap.superadmin.username:superadmin}")
    private String superAdminUsername;

    @Value("${app.bootstrap.superadmin.email:superadmin@shopmanager.local}")
    private String superAdminEmail;

    @Value("${app.bootstrap.superadmin.firstname:Super}")
    private String superAdminFirstName;

    @Value("${app.bootstrap.superadmin.lastname:Admin}")
    private String superAdminLastName;

    @Value("${app.bootstrap.superadmin.phonenumber:1-000-000-0000}")
    private String superAdminPhoneNumber;

    @Value("${app.bootstrap.superadmin.password:changeme}")
    private String superAdminPassword;

    /**
     * Bootstrap super admin user after application is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void bootstrapSuperAdmin() {
        if (!bootstrapEnabled) {
            log.info("Super admin bootstrap is disabled");
            return;
        }

        log.info("Checking for existing super admin users...");

        // Check if any super admin already exists
        if (hasSuperAdminUser()) {
            log.info("Super admin user already exists, skipping bootstrap");
            return;
        }

        log.info("No super admin found, creating bootstrap super admin user");

        try {
            createBootstrapSuperAdmin();
            log.info("Bootstrap super admin user created successfully");

        } catch (Exception e) {
            log.error("Failed to create bootstrap super admin user", e);
            // Don't throw exception to avoid application startup failure
        }
    }

    /**
     * Check if any super admin user exists
     */
    private boolean hasSuperAdminUser() {
        Optional<Role> superAdminRole = roleRepository.findByName(ROLE_SYSTEM_ADMIN);
        if (superAdminRole.isEmpty()) {
            log.warn("SYSTEM_ADMIN role not found in database");
            return false;
        }

        long superAdminCount = userRepository.countByRolesContaining(superAdminRole.get());
        log.info("Found {} existing super admin users", superAdminCount);
        return superAdminCount > 0;
    }

    /**
     * Create the bootstrap super admin user
     */
    private void createBootstrapSuperAdmin() {
        // Get or create SYSTEM_ADMIN role
        Role superAdminRole = roleRepository.findByName(ROLE_SYSTEM_ADMIN)
            .orElseThrow(() -> new IllegalStateException("SYSTEM_ADMIN role not found. Please ensure database migrations have been applied."));

        // Generate password if not provided
        String finalPassword = superAdminPassword.isEmpty()
            ? keycloakUserService.generatePassword()
            : superAdminPassword;

        // Create user in Keycloak first
        String keycloakId = createKeycloakSuperAdmin(finalPassword);

        // Create user entity in database
        User superAdminUser = User.builder()
            .username(superAdminUsername)
            .email(superAdminEmail)
            .firstName(superAdminFirstName)
            .lastName(superAdminLastName)
            .phoneNumber(superAdminPhoneNumber)
            .keycloakId(keycloakId)
            .status(User.UserStatus.ACTIVE)
            .roles(Set.of(superAdminRole))
            .build();

        userRepository.save(superAdminUser);

        // Log the bootstrap event
        auditService.logEvent(
            "SYSTEM_ADMIN_BOOTSTRAP",
            "Bootstrap super admin user created: " + superAdminUsername,
            Map.of(
                "username", superAdminUsername,
                "email", superAdminEmail,
                "keycloakId", keycloakId
            )
        );

        log.info("Bootstrap super admin created - Username: {}, Email: {}, Generated Password: {}",
                superAdminUsername,
                superAdminEmail,
                superAdminPassword.isEmpty() ? "[GENERATED - CHECK LOGS]" : "[CONFIGURED]");

        if (superAdminPassword.isEmpty()) {
            log.warn("Generated password for super admin '{}': {}", superAdminUsername, finalPassword);
            log.warn("Please change this password after first login!");
        }
    }

    /**
     * Create super admin user in Keycloak
     */
    private String createKeycloakSuperAdmin(String password) {
        CreateKeycloakUserRequest keycloakRequest = CreateKeycloakUserRequest.forSuperAdmin(
            superAdminUsername,
            superAdminEmail,
            superAdminFirstName,
            superAdminLastName,
            superAdminPhoneNumber,
            password
        );

        return keycloakUserService.createUser(keycloakRequest);
    }

    /**
     * Check if bootstrap is enabled and required
     */
    public boolean isBootstrapRequired() {
        return bootstrapEnabled && !hasSuperAdminUser();
    }

    /**
     * Get current super admin count
     */
    public long getSuperAdminCount() {
        Optional<Role> superAdminRole = roleRepository.findByName(ROLE_SYSTEM_ADMIN);
        if (superAdminRole.isEmpty()) {
            return 0;
        }
        return userRepository.countByRolesContaining(superAdminRole.get());
    }
}