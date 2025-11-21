package com.princely.shopmanager.core.service;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.dto.ShopCreateRequest;
import com.princely.shopmanager.core.dto.ShopResponse;
import com.princely.shopmanager.core.dto.ShopUpdateRequest;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.statemachine.ShopStatusStateMachine;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.shared.events.ShopCreatedEvent;
import com.princely.shopmanager.shared.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing shop operations.
 *
 * This service provides comprehensive shop management functionality including:
 * - CRUD operations for shops with proper validation
 * - Multi-tenant context handling and isolation
 * - Automatic tenant ID generation and management
 * - Audit logging for all shop operations
 * - Status management and business rule enforcement
 *
 * All operations respect tenant boundaries and include proper authorization checks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@CacheConfig(cacheNames = "shops")
public class ShopService {

    private final ShopRepository shopRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;
    private final ShopStatusStateMachine stateMachine;
    private final com.princely.shopmanager.shared.security.TenantSecurityValidator tenantSecurityValidator;

    /**
     * Creates a new shop for the current tenant.
     *
     * This method:
     * - Retrieves the tenant ID from the authenticated user's context
     * - Associates the shop with the user's existing tenant
     * - Creates the shop entity with proper defaults
     * - Saves to database with audit logging
     *
     * @param request Shop creation request with validation
     * @return Created shop response DTO
     * @throws IllegalArgumentException if shop name already exists
     * @throws IllegalStateException if no tenant context or tenant not found
     */
    @Transactional
    @CacheEvict(key = "'active-shops-' + T(com.princely.shopmanager.auth.context.TenantContext).getCurrentTenantId()")
    public ShopResponse createShop(ShopCreateRequest request) {
        log.info("Creating new shop: {}", request.getName());

        // Check if shop name already exists
        if (shopRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Shop with name '" + request.getName() + "' already exists");
        }

        // Get tenant ID from logged-in user's context
        String tenantId = TenantContext.requireCurrentTenant();
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalStateException("Tenant not found: " + tenantId));

        // Build shop entity
        Shop shop = Shop.builder()
            .name(request.getName())
            .description(request.getDescription())
            .address(request.getAddress())
            .city(request.getCity())
            .state(request.getState())
            .country(request.getCountry())
            .postalCode(request.getPostalCode())
            .phoneNumber(request.getPhoneNumber())
            .email(request.getEmail())
            .taxId(request.getTaxId())
            .status(Shop.ShopStatus.ACTIVE)
            .openingDate(request.getOpeningDate() != null ? request.getOpeningDate() : LocalDateTime.now())
            .tenant(tenant)
            .build();

        shop = shopRepository.save(shop);

        // Audit the creation
        auditService.logEntityCreation("Shop", shop.getId(),
            "Shop created: " + shop.getName() + " for tenant ID: " + tenant.getId());

        // Publish shop created event
        eventPublisher.publishEvent(new ShopCreatedEvent(shop.getId(), tenant.getId(), shop.getName()));

        log.info("Successfully created shop with ID: {} for tenant ID: {}", shop.getId(), tenant.getId());
        return ShopResponse.fromEntity(shop);
    }

    /**
     * Updates an existing shop with partial update support.
     *
     * This method:
     * - Validates shop exists and user has access
     * - Applies only non-null fields from update request
     * - Maintains audit trail of changes
     * - Respects tenant isolation boundaries
     *
     * @param shopId ID of shop to update
     * @param request Update request with optional fields
     * @return Updated shop response DTO
     * @throws IllegalArgumentException if shop not found or access denied
     */
    @Transactional
    @Caching(evict = {
        @CacheEvict(key = "#shopId"),
        @CacheEvict(key = "'active-shops-' + T(com.princely.shopmanager.auth.context.TenantContext).getCurrentTenantId()")
    })
    public ShopResponse updateShop(String shopId, ShopUpdateRequest request) {
        log.info("Updating shop: {}", shopId);

        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + shopId));

        // Verify tenant access using centralized validator
        tenantSecurityValidator.validateShopAccess(shop);

        // Store original values for audit
        String originalName = shop.getName();
        Shop.ShopStatus originalStatus = shop.getStatus();

        // Apply updates
        request.applyTo(shop);

        shop = shopRepository.save(shop);

        // Audit significant changes
        if (!originalName.equals(shop.getName())) {
            auditService.logEntityModification("Shop", shop.getId(),
                "Shop name changed from '" + originalName + "' to '" + shop.getName() + "'");
        }
        if (!originalStatus.equals(shop.getStatus())) {
            auditService.logEntityModification("Shop", shop.getId(),
                "Shop status changed from " + originalStatus + " to " + shop.getStatus());
        }

        log.info("Successfully updated shop: {}", shopId);
        return ShopResponse.fromEntity(shop);
    }

    /**
     * Retrieves a shop by ID with tenant access validation.
     *
     * @param shopId ID of shop to retrieve
     * @return Shop response DTO if found and accessible
     * @throws IllegalArgumentException if shop not found or access denied
     */
    @Transactional(readOnly = true)
    @Cacheable(key = "#shopId", condition = "#shopId != null")
    public ShopResponse getShop(String shopId) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + shopId));

        // Verify tenant access using centralized validator
        tenantSecurityValidator.validateShopAccess(shop);

        return ShopResponse.fromEntity(shop);
    }

    /**
     * Retrieves shops accessible to the current user with pagination.
     *
     * Returns only shops within the user's tenant. Tenant context is required.
     * For system admin access to all shops, use getAllShopsSystemAdmin() instead.
     *
     * @param pageable Pagination parameters
     * @return Page of shop response DTOs
     * @throws IllegalStateException if no tenant context is available
     */
    @Transactional(readOnly = true)
    public Page<ShopResponse> getShops(Pageable pageable) {
        // Require tenant context - system admins should use getAllShopsSystemAdmin()
        String currentTenantId = TenantContext.requireCurrentTenant();

        Page<Shop> shops = shopRepository.findByTenant_Id(currentTenantId, pageable);
        log.debug("Retrieved {} shops for tenant: {}", shops.getContent().size(), currentTenantId);

        return shops.map(ShopResponse::fromEntity);
    }

    /**
     * Retrieves ALL shops across ALL tenants (System Admin only).
     *
     * This method explicitly bypasses tenant isolation and returns all shops
     * in the system. Should only be called from endpoints restricted to SYSTEM_ADMIN.
     *
     * @param pageable Pagination parameters
     * @return Page of all shop response DTOs
     */
    @Transactional(readOnly = true)
    public Page<ShopResponse> getAllShopsSystemAdmin(Pageable pageable) {
        log.debug("System admin retrieving all shops across all tenants");
        Page<Shop> shops = shopRepository.findAll(pageable);
        log.debug("Retrieved {} shops total", shops.getTotalElements());
        return shops.map(ShopResponse::fromEntity);
    }

    /**
     * Retrieves all active shops for the current tenant.
     *
     * @return List of active shop response DTOs
     */
    @Transactional(readOnly = true)
    @Cacheable(key = "'active-shops-' + T(com.princely.shopmanager.auth.context.TenantContext).getCurrentTenantId()")
    public List<ShopResponse> getActiveShops() {
        String currentTenantId = TenantContext.getCurrentTenantId();

        List<Shop> shops;
        if (currentTenantId == null) {
            shops = shopRepository.findByStatus(Shop.ShopStatus.ACTIVE);
        } else {
            shops = shopRepository.findByTenant_IdAndStatus(currentTenantId, Shop.ShopStatus.ACTIVE);
        }

        return shops.stream()
            .map(ShopResponse::fromEntity)
            .toList();
    }

    /**
     * Changes the status of a shop (e.g., suspend, reactivate, close).
     *
     * This method:
     * - Validates the status transition is allowed
     * - Updates the shop status
     * - Creates audit log entry
     * - Handles business rules for status changes
     *
     * @param shopId ID of shop to update
     * @param newStatus New status to apply
     * @return Updated shop response DTO
     * @throws IllegalArgumentException if shop not found or invalid status transition
     */
    @Transactional
    @Caching(evict = {
        @CacheEvict(key = "#shopId"),
        @CacheEvict(key = "'active-shops-' + T(com.princely.shopmanager.auth.context.TenantContext).getCurrentTenantId()")
    })
    public ShopResponse changeShopStatus(String shopId, Shop.ShopStatus newStatus) {
        log.info("Changing shop status: {} to {}", shopId, newStatus);

        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + shopId));

        // Verify tenant access using centralized validator
        tenantSecurityValidator.validateShopAccess(shop);

        Shop.ShopStatus originalStatus = shop.getStatus();

        // Validate status transition using state machine
        stateMachine.validateTransition(originalStatus, newStatus, shopId);

        shop.setStatus(newStatus);
        shop = shopRepository.save(shop);

        // Audit the status change
        auditService.logEntityModification("Shop", shop.getId(),
            "Shop status changed from " + originalStatus + " to " + newStatus);

        log.info("Successfully changed shop {} status to {}", shopId, newStatus);
        return ShopResponse.fromEntity(shop);
    }

    /**
     * Deletes a shop (soft delete by setting status to CLOSED).
     *
     * This method implements soft delete to preserve historical data and relationships.
     * Physical deletion is not allowed to maintain data integrity and audit trails.
     *
     * @param shopId ID of shop to delete
     * @throws IllegalArgumentException if shop not found or access denied
     */
    @Transactional
    @Caching(evict = {
        @CacheEvict(key = "#shopId"),
        @CacheEvict(key = "'active-shops-' + T(com.princely.shopmanager.auth.context.TenantContext).getCurrentTenantId()")
    })
    public void deleteShop(String shopId) {
        log.info("Deleting shop: {}", shopId);

        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + shopId));

        // Verify tenant access using centralized validator
        tenantSecurityValidator.validateShopAccess(shop);

        // Soft delete by changing status
        shop.setStatus(Shop.ShopStatus.CLOSED);
        shopRepository.save(shop);

        // Audit the deletion
        auditService.logEntityDeletion("Shop", shop.getId(),
            "Shop soft deleted: " + shop.getName());

        log.info("Successfully deleted shop: {}", shopId);
    }

}