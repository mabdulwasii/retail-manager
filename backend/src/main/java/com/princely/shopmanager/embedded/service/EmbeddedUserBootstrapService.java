package com.princely.shopmanager.embedded.service;

import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.shared.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

import static com.princely.shopmanager.auth.constants.SecurityRoles.TENANT_ADMIN;

/**
 * Service responsible for bootstrapping additional users in embedded mode.
 * Creates tenant admin and other default users for standalone deployments.
 * Runs AFTER SuperAdminBootstrapService to ensure superadmin exists first.
 */
@Service
@Profile("embedded")
@RequiredArgsConstructor
@Slf4j
public class EmbeddedUserBootstrapService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Value("${app.bootstrap.users.enabled:true}")
    private boolean bootstrapEnabled;

    @Value("${app.bootstrap.tenantadmin.username:admin}")
    private String tenantAdminUsername;

    @Value("${app.bootstrap.tenantadmin.email:admin@shopmanager.local}")
    private String tenantAdminEmail;

    @Value("${app.bootstrap.tenantadmin.firstname:Tenant}")
    private String tenantAdminFirstName;

    @Value("${app.bootstrap.tenantadmin.lastname:Admin}")
    private String tenantAdminLastName;

    @Value("${app.bootstrap.tenantadmin.phonenumber:1-111-111-1111}")
    private String tenantAdminPhoneNumber;

    @Value("${app.bootstrap.tenantadmin.password:admin123}")
    private String tenantAdminPassword;

    /**
     * Bootstrap additional users after application is ready.
     * Runs with Order 100 to execute AFTER SuperAdminBootstrapService.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Order(100)
    @Transactional
    public void bootstrapUsers() {
        if (!bootstrapEnabled) {
            log.info("Embedded user bootstrap is disabled");
            return;
        }

        log.info("Checking for existing tenant admin user...");

        // Check if tenant admin already exists
        if (userRepository.findByUsername(tenantAdminUsername).isPresent()) {
            log.info("Tenant admin user '{}' already exists, skipping bootstrap", tenantAdminUsername);
            return;
        }

        log.info("No tenant admin found, creating bootstrap tenant admin user");

        try {
            createTenantAdmin();
            log.info("Bootstrap tenant admin user created successfully");

        } catch (Exception e) {
            log.error("Failed to create bootstrap tenant admin user", e);
            // Don't throw exception to avoid application startup failure
        }
    }

    /**
     * Create the bootstrap tenant admin user
     */
    private void createTenantAdmin() {
        // Get TENANT_ADMIN role
        Role tenantAdminRole = roleRepository.findByName(TENANT_ADMIN)
                .orElseThrow(() -> new IllegalStateException("TENANT_ADMIN role not found. Please ensure database migrations have been applied."));

        // Get default tenant and shop
        Tenant defaultTenant = tenantRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No tenant found. EmbeddedTenantBootstrapService should have created one."));

        Shop defaultShop = shopRepository.findByTenantId(defaultTenant.getId()).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No shop found for tenant. EmbeddedTenantBootstrapService should have created one."));

        log.info("Creating tenant admin for tenant '{}' and shop '{}'",
                defaultTenant.getName(), defaultShop.getName());

        // Create tenant admin user
        User tenantAdmin = User.builder()
                .username(tenantAdminUsername)
                .email(tenantAdminEmail)
                .firstName(tenantAdminFirstName)
                .lastName(tenantAdminLastName)
                .phoneNumber(tenantAdminPhoneNumber)
                .passwordHash(passwordEncoder.encode(tenantAdminPassword))
                .status(User.UserStatus.ACTIVE)
                .roles(Set.of(tenantAdminRole))
                .tenant(defaultTenant)
                .shop(defaultShop)
                .build();

        userRepository.save(tenantAdmin);

        // Log the bootstrap event
        auditService.logEvent(
                "TENANT_ADMIN_BOOTSTRAP",
                "Bootstrap tenant admin user created: " + tenantAdminUsername,
                Map.of(
                        "username", tenantAdminUsername,
                        "email", tenantAdminEmail,
                        "tenantId", defaultTenant.getId(),
                        "shopId", defaultShop.getId(),
                        "mode", "embedded"
                )
        );

        log.info("Bootstrap tenant admin created - Username: {}, Email: {}, Password: [CONFIGURED]",
                tenantAdminUsername,
                tenantAdminEmail);
        log.warn("Password for tenant admin '{}': {}", tenantAdminUsername, tenantAdminPassword);
        log.warn("Please change this password after first login!");
    }

    /**
     * Check if bootstrap is required
     */
    public boolean isBootstrapRequired() {
        return bootstrapEnabled && userRepository.findByUsername(tenantAdminUsername).isEmpty();
    }
}
