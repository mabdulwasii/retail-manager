package com.princely.shopmanager.core.service;

import com.princely.shopmanager.auth.dto.CreateKeycloakUserRequest;
import com.princely.shopmanager.auth.service.UserManagementService;
import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.shared.service.AuditService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.princely.shopmanager.auth.constants.SecurityRoles.SYSTEM_ADMIN;

/**
 * Service responsible for bootstrapping super admin user on application startup
 */
@Service
@Slf4j
public class SuperAdminBootstrapService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditService auditService;
    private final TenantRepository tenantRepository;
    private final ShopRepository shopRepository;
    private final Environment environment;

    // Optional dependencies (not available in embedded mode)
    @Autowired(required = false)
    private UserManagementService userManagementService;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    public SuperAdminBootstrapService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            AuditService auditService,
            TenantRepository tenantRepository,
            ShopRepository shopRepository,
            Environment environment) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.auditService = auditService;
        this.tenantRepository = tenantRepository;
        this.shopRepository = shopRepository;
        this.environment = environment;
    }

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
        Optional<Role> superAdminRole = roleRepository.findByName(SYSTEM_ADMIN);
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
        Role superAdminRole = roleRepository.findByName(SYSTEM_ADMIN)
            .orElseThrow(() -> new IllegalStateException("SYSTEM_ADMIN role not found. Please ensure database migrations have been applied."));

        // Check if running in embedded mode
        boolean isEmbeddedMode = Arrays.asList(environment.getActiveProfiles()).contains("embedded");

        // Generate password if not provided
        String finalPassword = superAdminPassword.isEmpty()
            ? (isEmbeddedMode ? "changeme" : userManagementService.generatePassword())
            : superAdminPassword;

        // Get default tenant and shop for embedded mode
        Tenant defaultTenant = null;
        Shop defaultShop = null;
        if (isEmbeddedMode) {
            // In embedded mode, use the first available tenant and shop
            defaultTenant = tenantRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No tenant found. EmbeddedTenantBootstrapService should have created one."));

            defaultShop = shopRepository.findByTenantId(defaultTenant.getId()).stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No shop found for tenant. EmbeddedTenantBootstrapService should have created one."));

            log.info("Using default tenant '{}' and shop '{}' for super admin",
                    defaultTenant.getName(), defaultShop.getName());
        }

        // Create user entity
        User.UserBuilder userBuilder = User.builder()
            .username(superAdminUsername)
            .email(superAdminEmail)
            .firstName(superAdminFirstName)
            .lastName(superAdminLastName)
            .phoneNumber(superAdminPhoneNumber)
            .status(User.UserStatus.ACTIVE)
            .roles(Set.of(superAdminRole))
            .tenant(defaultTenant)
            .shop(defaultShop);

        String keycloakId = null;
        if (isEmbeddedMode) {
            // Embedded mode: Set password hash, no Keycloak
            if (passwordEncoder == null) {
                throw new IllegalStateException("PasswordEncoder is required for embedded mode");
            }
            userBuilder.passwordHash(passwordEncoder.encode(finalPassword));
            log.info("Creating super admin with password hash for embedded mode");
        } else {
            // Cloud mode: Create in Keycloak
            keycloakId = createKeycloakSuperAdmin(finalPassword);
            userBuilder.keycloakId(keycloakId);
            log.info("Creating super admin with Keycloak ID for cloud mode");
        }

        User superAdminUser = userBuilder.build();
        userRepository.save(superAdminUser);

        // Log the bootstrap event
        Map<String, Object> auditData = Map.of(
            "username", superAdminUsername,
            "email", superAdminEmail,
            "mode", isEmbeddedMode ? "embedded" : "cloud",
            "keycloakId", keycloakId != null ? keycloakId : "N/A"
        );

        auditService.logEvent(
            "SYSTEM_ADMIN_BOOTSTRAP",
            "Bootstrap super admin user created: " + superAdminUsername,
            auditData
        );

        log.info("Bootstrap super admin created - Username: {}, Email: {}, Mode: {}, Password: {}",
                superAdminUsername,
                superAdminEmail,
                isEmbeddedMode ? "EMBEDDED" : "CLOUD",
                superAdminPassword.isEmpty() ? "[GENERATED - CHECK LOGS]" : "[CONFIGURED]");

        if (superAdminPassword.isEmpty() || isEmbeddedMode) {
            log.warn("Password for super admin '{}': {}", superAdminUsername, finalPassword);
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

        return userManagementService.createUser(keycloakRequest);
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
        Optional<Role> superAdminRole = roleRepository.findByName(SYSTEM_ADMIN);
        if (superAdminRole.isEmpty()) {
            return 0;
        }
        return userRepository.countByRolesContaining(superAdminRole.get());
    }
}