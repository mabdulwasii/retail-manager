package com.princely.shopmanager.embedded.service;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.shared.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service responsible for bootstrapping default tenant and shop in embedded mode.
 * Runs BEFORE SuperAdminBootstrapService to ensure tenant exists for user creation.
 */
@Service
@Profile("embedded")
@RequiredArgsConstructor
@Slf4j
public class EmbeddedTenantBootstrapService {

    private final TenantRepository tenantRepository;
    private final ShopRepository shopRepository;
    private final AuditService auditService;

    @Value("${app.bootstrap.tenant.enabled:true}")
    private boolean bootstrapEnabled;

    @Value("${app.bootstrap.tenant.name:Default Organization}")
    private String defaultTenantName;

    @Value("${app.bootstrap.tenant.email:contact@shopmanager.local}")
    private String defaultTenantEmail;

    @Value("${app.bootstrap.tenant.address:123 Main Street}")
    private String defaultTenantAddress;

    @Value("${app.bootstrap.tenant.city:Springfield}")
    private String defaultTenantCity;

    @Value("${app.bootstrap.tenant.country:USA}")
    private String defaultTenantCountry;

    @Value("${app.bootstrap.shop.name:Main Shop}")
    private String defaultShopName;

    @Value("${app.bootstrap.shop.address:123 Main Street}")
    private String defaultShopAddress;

    @Value("${app.bootstrap.shop.city:Springfield}")
    private String defaultShopCity;

    @Value("${app.bootstrap.shop.country:USA}")
    private String defaultShopCountry;

    @Value("${app.bootstrap.shop.phone:1-800-000-0000}")
    private String defaultShopPhone;

    @Value("${app.bootstrap.shop.email:shop@shopmanager.local}")
    private String defaultShopEmail;

    /**
     * Bootstrap default tenant and shop after application is ready.
     * Runs with high priority (Order 0) to execute before SuperAdminBootstrapService.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Order(0)  // Execute before SuperAdminBootstrapService (which has default order)
    @Transactional
    public void bootstrapTenantAndShop() {
        if (!bootstrapEnabled) {
            log.info("Embedded tenant bootstrap is disabled");
            return;
        }

        log.info("Checking for existing tenant in embedded mode...");

        // Check if any tenant exists
        if (tenantRepository.count() > 0) {
            log.info("Tenant already exists, skipping bootstrap");
            return;
        }

        log.info("No tenant found, creating default tenant and shop for embedded mode");

        try {
            createDefaultTenantAndShop();
            log.info("Default tenant and shop created successfully");

        } catch (Exception e) {
            log.error("Failed to create default tenant and shop", e);
            // Don't throw exception to avoid application startup failure
        }
    }

    /**
     * Create default tenant and shop for embedded mode
     */
    private void createDefaultTenantAndShop() {
        // Create default tenant
        Tenant tenant = Tenant.builder()
                .name(defaultTenantName)
                .description("Auto-generated tenant for embedded mode deployment")
                .contactEmail(defaultTenantEmail)
                .primaryAddress(defaultTenantAddress)
                .city(defaultTenantCity)
                .country(defaultTenantCountry)
                .status(Tenant.TenantStatus.ACTIVE)
                .createdDate(LocalDateTime.now())
                .build();

        tenant = tenantRepository.save(tenant);
        log.info("Created default tenant: {} (ID: {})", tenant.getName(), tenant.getId());

        // Create default shop
        Shop shop = Shop.builder()
                .tenant(tenant)
                .name(defaultShopName)
                .description("Auto-generated shop for embedded mode deployment")
                .address(defaultShopAddress)
                .city(defaultShopCity)
                .country(defaultShopCountry)
                .phoneNumber(defaultShopPhone)
                .email(defaultShopEmail)
                .status(Shop.ShopStatus.ACTIVE)
                .openingDate(LocalDateTime.now())
                .build();

        shop = shopRepository.save(shop);
        log.info("Created default shop: {} (ID: {})", shop.getName(), shop.getId());

        // Log the bootstrap event
        auditService.logEvent(
            "EMBEDDED_TENANT_BOOTSTRAP",
            "Default tenant and shop created for embedded mode",
            Map.of(
                "tenantId", tenant.getId(),
                "tenantName", tenant.getName(),
                "shopId", shop.getId(),
                "shopName", shop.getName()
            )
        );

        log.info("Embedded mode bootstrap complete - Tenant: '{}', Shop: '{}'",
                tenant.getName(),
                shop.getName());
    }

    /**
     * Get the default tenant (creates if not exists)
     */
    @Transactional
    public Tenant getOrCreateDefaultTenant() {
        return tenantRepository.findByName(defaultTenantName)
                .orElseGet(() -> {
                    log.warn("Default tenant not found, creating it now");
                    createDefaultTenantAndShop();
                    return tenantRepository.findByName(defaultTenantName)
                            .orElseThrow(() -> new IllegalStateException("Failed to create default tenant"));
                });
    }

    /**
     * Get the default shop (creates if not exists)
     */
    @Transactional
    public Shop getOrCreateDefaultShop() {
        return shopRepository.findByName(defaultShopName)
                .orElseGet(() -> {
                    log.warn("Default shop not found, creating it now");
                    createDefaultTenantAndShop();
                    return shopRepository.findByName(defaultShopName)
                            .orElseThrow(() -> new IllegalStateException("Failed to create default shop"));
                });
    }

    /**
     * Check if bootstrap is required
     */
    public boolean isBootstrapRequired() {
        return bootstrapEnabled && tenantRepository.count() == 0;
    }
}
