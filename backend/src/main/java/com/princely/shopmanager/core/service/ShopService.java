package com.princely.shopmanager.core.service;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.dto.ShopCreateRequest;
import com.princely.shopmanager.core.dto.ShopResponse;
import com.princely.shopmanager.core.dto.ShopUpdateRequest;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.shared.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class ShopService {

    private final ShopRepository shopRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    /**
     * Creates a new shop with automatic tenant ID generation.
     *
     * This method:
     * - Generates a unique tenant ID based on shop name
     * - Creates the shop entity with proper defaults
     * - Saves to database with audit logging
     * - Sets up default shop configuration
     *
     * @param request Shop creation request with validation
     * @return Created shop response DTO
     * @throws IllegalArgumentException if shop name already exists
     */
    @Transactional
    public ShopResponse createShop(ShopCreateRequest request) {
        log.info("Creating new shop: {}", request.getName());

        // Check if shop name already exists
        if (shopRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Shop with name '" + request.getName() + "' already exists");
        }

        // Create or get tenant
        Tenant tenant = getOrCreateTenant(request.getName());

        // Create shop entity
        Shop shop = Shop.builder()
            .name(request.getName())
            .tenant(tenant)
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
            .build();

        shop = shopRepository.save(shop);

        // Audit the creation
        auditService.logEntityCreation("Shop", shop.getId(),
            "Shop created: " + shop.getName() + " with tenant ID: " + tenant.getId());

        log.info("Successfully created shop with ID: {} and tenant ID: {}", shop.getId(), tenant.getId());
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
    public ShopResponse updateShop(String shopId, ShopUpdateRequest request) {
        log.info("Updating shop: {}", shopId);

        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + shopId));

        // Verify tenant access
        String currentTenantId = TenantContext.getCurrentTenantId();
        if (currentTenantId != null && !currentTenantId.equals(shop.getTenant().getId())) {
            throw new IllegalArgumentException("Access denied to shop: " + shopId);
        }

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
    public ShopResponse getShop(String shopId) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + shopId));

        // Verify tenant access
        String currentTenantId = TenantContext.getCurrentTenantId();
        if (currentTenantId != null && !currentTenantId.equals(shop.getTenant().getId())) {
            throw new IllegalArgumentException("Access denied to shop: " + shopId);
        }

        return ShopResponse.fromEntity(shop);
    }

    /**
     * Retrieves shops accessible to the current user with pagination.
     *
     * For system administrators, returns all shops.
     * For tenant users, returns only shops within their tenant context.
     *
     * @param pageable Pagination parameters
     * @return Page of shop response DTOs
     */
    @Transactional(readOnly = true)
    public Page<ShopResponse> getShops(Pageable pageable) {
        String currentTenantId = TenantContext.getCurrentTenantId();

        Page<Shop> shops;
        if (currentTenantId == null) {
            // System admin - can see all shops
            shops = shopRepository.findAll(pageable);
            log.debug("Retrieved {} shops for system admin", shops.getContent().size());
        } else {
            // Tenant user - can only see shops in their tenant
            shops = shopRepository.findByTenant_Id(currentTenantId, pageable);
            log.debug("Retrieved {} shops for tenant: {}", shops.getContent().size(), currentTenantId);
        }

        return shops.map(ShopResponse::fromEntity);
    }

    /**
     * Retrieves all active shops for the current tenant.
     *
     * @return List of active shop response DTOs
     */
    @Transactional(readOnly = true)
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
    public ShopResponse changeShopStatus(String shopId, Shop.ShopStatus newStatus) {
        log.info("Changing shop status: {} to {}", shopId, newStatus);

        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + shopId));

        // Verify tenant access
        String currentTenantId = TenantContext.getCurrentTenantId();
        if (currentTenantId != null && !currentTenantId.equals(shop.getTenant().getId())) {
            throw new IllegalArgumentException("Access denied to shop: " + shopId);
        }

        Shop.ShopStatus originalStatus = shop.getStatus();

        // Validate status transition
        validateStatusTransition(originalStatus, newStatus);

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
    public void deleteShop(String shopId) {
        log.info("Deleting shop: {}", shopId);

        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + shopId));

        // Verify tenant access
        String currentTenantId = TenantContext.getCurrentTenantId();
        if (currentTenantId != null && !currentTenantId.equals(shop.getTenant().getId())) {
            throw new IllegalArgumentException("Access denied to shop: " + shopId);
        }

        // Soft delete by changing status
        shop.setStatus(Shop.ShopStatus.CLOSED);
        shopRepository.save(shop);

        // Audit the deletion
        auditService.logEntityDeletion("Shop", shop.getId(),
            "Shop soft deleted: " + shop.getName());

        log.info("Successfully deleted shop: {}", shopId);
    }

    /**
     * Creates or retrieves a tenant based on shop name.
     *
     * @param shopName Name of the shop
     * @return Tenant entity
     */
    private Tenant getOrCreateTenant(String shopName) {
        String baseTenantId = shopName.toLowerCase()
            .replaceAll("[^a-z0-9]", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");

        // Ensure uniqueness by appending UUID suffix if needed
        String tenantId = "tenant-" + baseTenantId;
        if (tenantRepository.existsById(tenantId)) {
            tenantId = tenantId + "-" + UUID.randomUUID().toString().substring(0, 8);
        }

        // Create new tenant first
        Tenant tenant = tenantRepository.save(Tenant.builder()
            .id(tenantId)
            .name(shopName + " Organization")
            .contactEmail("admin@" + baseTenantId.replaceAll("-", "") + ".com")
            .status(Tenant.TenantStatus.ACTIVE)
            .build());

        // Create contact user for the tenant
        String contactEmail = "admin@" + baseTenantId.replaceAll("-", "") + ".com";
        User contactUser = userRepository.save(User.builder()
            .tenant(tenant)
            .keycloakId("admin-" + tenantId)
            .username("admin-" + shopName.toLowerCase().replaceAll("[^a-z0-9]", ""))
            .email(contactEmail)
            .firstName("Admin")
            .lastName("User")
            .status(User.UserStatus.ACTIVE)
            .build());

        // Update tenant with contact user reference
        tenant.setContactUser(contactUser);
        return tenantRepository.save(tenant);
    }

    /**
     * Validates if a status transition is allowed based on business rules.
     *
     * @param currentStatus Current shop status
     * @param newStatus Desired new status
     * @throws IllegalArgumentException if transition is not allowed
     */
    private void validateStatusTransition(Shop.ShopStatus currentStatus, Shop.ShopStatus newStatus) {
        // Business rules for status transitions
        switch (currentStatus) {
            case CLOSED:
                throw new IllegalArgumentException("Cannot change status of closed shop");
            case SUSPENDED:
                if (newStatus == Shop.ShopStatus.CLOSED) {
                    throw new IllegalArgumentException("Cannot close suspended shop directly - must reactivate first");
                }
                break;
            case INACTIVE:
                // Inactive shops can transition to any status
                break;
            case ACTIVE:
                // Active shops can transition to any status
                break;
        }
    }
}