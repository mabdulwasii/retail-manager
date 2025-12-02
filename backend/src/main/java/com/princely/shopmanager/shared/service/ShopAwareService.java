package com.princely.shopmanager.shared.service;

import com.princely.shopmanager.auth.security.ShopAccessValidator;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.shared.domain.ShopAware;
import com.princely.shopmanager.shared.exception.ShopNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.AccessDeniedException;

/**
 * Abstract base service class providing standard shop access validation methods.
 *
 * <p>Services managing shop-scoped resources should extend this class to inherit
 * consistent access control patterns.
 *
 * <p>This class provides:
 * <ul>
 *   <li>Shop existence validation</li>
 *   <li>Shop access validation based on user roles</li>
 *   <li>Entity retrieval with shop access validation</li>
 *   <li>Tenant-wide access checking</li>
 * </ul>
 *
 * <p>Access rules enforced:
 * <ul>
 *   <li>SYSTEM_ADMIN: Access to all shops across all tenants</li>
 *   <li>TENANT_ADMIN: Access to all shops within their tenant</li>
 *   <li>OWNER: Access to all shops within their tenant</li>
 *   <li>Regular users (MANAGER, EMPLOYEE, etc.): Access only to their assigned shop</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>{@code
 * @Service
 * public class ProductService extends ShopAwareService {
 *
 *     private final ProductRepository productRepository;
 *
 *     public ProductResponse getProduct(String productId, JwtPrincipal principal) {
 *         Product product = findEntityWithShopAccess(
 *             productId,
 *             productRepository,
 *             principal,
 *             "Product"
 *         );
 *         return mapToResponse(product);
 *     }
 * }
 * }</pre>
 *
 * @see com.princely.shopmanager.auth.security.ShopAccessValidator
 * @see com.princely.shopmanager.shared.domain.ShopAware
 */
@Slf4j
public abstract class ShopAwareService {

    protected final ShopAccessValidator shopAccessValidator;
    protected final ShopRepository shopRepository;

    protected ShopAwareService(ShopAccessValidator shopAccessValidator, ShopRepository shopRepository) {
        this.shopAccessValidator = shopAccessValidator;
        this.shopRepository = shopRepository;
    }

    /**
     * Validates that a shop exists and the user has access to it.
     *
     * @param shopId    The shop ID to validate
     * @param principal The JWT principal containing user information
     * @throws ShopNotFoundException if shop does not exist
     * @throws AccessDeniedException if user does not have access to the shop
     */
    protected void validateShopAccess(String shopId, JwtPrincipal principal) {
        log.debug("Validating shop access for shop: {}, user: {}", shopId, principal.getUsername());

        // Step 1: Check if shop exists
        boolean shopExists = shopRepository.existsById(shopId);
        if (!shopExists) {
            log.warn("Shop not found: {}", shopId);
            throw new ShopNotFoundException("Shop with id " + shopId + " was not found");
        }

        // Step 2: Check user has access to shop
        if (shopAccessValidator.hasNoAccessToShop(shopId, principal)) {
            log.warn("Access denied for user {} to shop {}", principal.getUsername(), shopId);
            throw new AccessDeniedException("You don't have permission to access shop with id " + shopId);
        }

        log.debug("Shop access validated successfully for shop: {}, user: {}", shopId, principal.getUsername());
    }

    /**
     * Finds an entity by ID and validates that the user has access to its shop.
     *
     * <p>This method combines entity retrieval with shop access validation in a single operation.
     * It's the recommended pattern for all GET operations on shop-scoped entities.
     *
     * @param entityId   The entity ID to find
     * @param repository The repository to use for finding the entity
     * @param principal  The JWT principal containing user information
     * @param entityName The entity name for error messages (e.g., "Product", "Expense")
     * @param <T>        The entity type, must implement ShopAware
     * @param <ID>       The ID type
     * @return The entity if found and user has access
     * @throws jakarta.persistence.EntityNotFoundException if entity not found
     * @throws AccessDeniedException                       if user does not have access to the entity's shop
     */
    protected <T extends ShopAware, ID> T findEntityWithShopAccess(
            ID entityId,
            JpaRepository<T, ID> repository,
            JwtPrincipal principal,
            String entityName) {

        log.debug("Finding {} with shop access validation: {}, user: {}",
                entityName, entityId, principal.getUsername());

        // Step 1: Find the entity
        T entity = repository.findById(entityId)
                .orElseThrow(() -> {
                    log.warn("{} not found: {}", entityName, entityId);
                    return new jakarta.persistence.EntityNotFoundException(
                            entityName + " with id " + entityId + " was not found"
                    );
                });

        // Step 2: Validate shop access
        String shopId = entity.getShopId();
        if (shopAccessValidator.hasNoAccessToShop(shopId, principal)) {
            log.warn("Access denied for user {} to {} {} (shop: {})",
                    principal.getUsername(), entityName, entityId, shopId);
            throw new AccessDeniedException(
                    "You don't have permission to access this " + entityName
            );
        }

        log.debug("{} found and access validated: {}, shop: {}", entityName, entityId, shopId);
        return entity;
    }

    /**
     * Checks if the user has tenant-wide access privileges.
     *
     * <p>Users with tenant-wide access can access all shops within their tenant:
     * <ul>
     *   <li>SYSTEM_ADMIN</li>
     *   <li>TENANT_ADMIN</li>
     *   <li>OWNER</li>
     * </ul>
     *
     * @param principal The JWT principal containing user information
     * @return true if user has tenant-wide access, false otherwise
     */
    protected boolean hasTenantWideAccess(JwtPrincipal principal) {
        return shopAccessValidator.hasTenantWideAccess(principal);
    }

    /**
     * Checks if the user has access to a specific shop.
     *
     * @param shopId    The shop ID to check access for
     * @param principal The JWT principal containing user information
     * @return true if user has access, false otherwise
     */
    protected boolean hasAccessToShop(String shopId, JwtPrincipal principal) {
        return shopAccessValidator.hasAccess(shopId, principal);
    }
}
