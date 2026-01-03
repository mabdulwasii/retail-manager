package com.princely.shopmanager.aggregator.service;

import com.princely.shopmanager.aggregator.domain.CloudShop;
import com.princely.shopmanager.aggregator.domain.CloudTenant;
import com.princely.shopmanager.aggregator.dto.ShopLinkRequest;
import com.princely.shopmanager.aggregator.dto.ShopRegistrationDto;
import com.princely.shopmanager.aggregator.dto.TenantRegistrationRequest;
import com.princely.shopmanager.aggregator.dto.TenantRegistrationResponse;
import com.princely.shopmanager.aggregator.repository.CloudShopRepository;
import com.princely.shopmanager.aggregator.repository.CloudTenantRepository;
import com.princely.shopmanager.shared.exception.BusinessException;
import com.princely.shopmanager.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing cloud tenant registration and shop linking.
 * Handles registration of local embedded installations with the cloud aggregator.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CloudTenantService {

    private final CloudTenantRepository cloudTenantRepository;
    private final CloudShopRepository cloudShopRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register a new tenant from local embedded installation.
     * Generates an API key for future sync operations.
     *
     * @param request Tenant registration request
     * @return Registration response with cloud tenant ID and API key
     */
    @Transactional
    public TenantRegistrationResponse registerTenant(TenantRegistrationRequest request) {
        log.info("Registering new cloud tenant: {}", request.getTenantEmail());

        // Check if tenant already registered
        if (cloudTenantRepository.existsByTenantEmail(request.getTenantEmail())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Tenant with email " + request.getTenantEmail() + " is already registered");
        }

        // Validate shops list
        if (request.getShops() == null || request.getShops().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "At least one shop must be provided during registration");
        }

        // Generate API key (plain text - will be returned once to client)
        String apiKey = generateApiKey();
        String apiKeyHash = passwordEncoder.encode(apiKey);

        // Create cloud tenant
        CloudTenant cloudTenant = CloudTenant.builder()
                .tenantName(request.getTenantName())
                .tenantEmail(request.getTenantEmail())
                .companyRegistration(request.getCompanyRegistration())
                .taxId(request.getTaxId())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .phoneNumber(request.getPhoneNumber())
                .apiKeyHash(apiKeyHash)
                .status(CloudTenant.Status.ACTIVE)
                .shopCount(request.getShops().size())
                .subscriptionTier(CloudTenant.SubscriptionTier.FREE)
                .build();

        CloudTenant savedTenant = cloudTenantRepository.save(cloudTenant);
        log.info("Cloud tenant created with ID: {}", savedTenant.getId());

        // Create cloud shops
        List<CloudShop> shops = request.getShops().stream()
                .map(shopDto -> createCloudShop(savedTenant, shopDto))
                .toList();

        cloudShopRepository.saveAll(shops);
        log.info("Registered {} shops for cloud tenant {}", shops.size(), savedTenant.getId());

        // Return response with API key (only time it's returned in plain text)
        return TenantRegistrationResponse.builder()
                .cloudTenantId(savedTenant.getId())
                .apiKey(apiKey)
                .message("Tenant successfully registered with cloud aggregator")
                .registeredShopsCount(shops.size())
                .build();
    }

    /**
     * Link an additional shop to an existing cloud tenant.
     *
     * @param request Shop link request with API key for authentication
     * @param apiKey API key from request header (X-API-Key)
     */
    @Transactional
    public void linkShop(ShopLinkRequest request, String apiKey) {
        log.info("Linking new shop to cloud tenant: {}", request.getCloudTenantId());

        // Verify API key and get tenant
        CloudTenant cloudTenant = validateApiKeyAndGetTenant(apiKey, request.getCloudTenantId());

        // Create cloud shop
        CloudShop cloudShop = createCloudShop(cloudTenant, request.getShop());
        cloudShopRepository.save(cloudShop);

        // Increment shop count
        cloudTenant.incrementShopCount();
        cloudTenantRepository.save(cloudTenant);

        log.info("Successfully linked shop {} to cloud tenant {}", cloudShop.getShopName(),
                cloudTenant.getId());
    }

    /**
     * Unregister a cloud tenant and all associated shops.
     *
     * @param cloudTenantId Cloud tenant ID
     * @param apiKey API key for authentication
     */
    @Transactional
    public void unregisterTenant(String cloudTenantId, String apiKey) {
        log.info("Unregistering cloud tenant: {}", cloudTenantId);

        // Verify API key and get tenant
        CloudTenant cloudTenant = validateApiKeyAndGetTenant(apiKey, cloudTenantId);

        // Delete all shops
        List<CloudShop> shops = cloudShopRepository.findByCloudTenantId(cloudTenantId);
        cloudShopRepository.deleteAll(shops);
        log.info("Deleted {} shops for cloud tenant {}", shops.size(), cloudTenantId);

        // Delete tenant
        cloudTenantRepository.delete(cloudTenant);
        log.info("Cloud tenant {} successfully unregistered", cloudTenantId);
    }

    /**
     * Validate API key and retrieve cloud tenant.
     *
     * @param apiKey Plain text API key from request
     * @param cloudTenantId Expected cloud tenant ID
     * @return CloudTenant if validation succeeds
     * @throws BusinessException if validation fails
     */
    public CloudTenant validateApiKeyAndGetTenant(String apiKey, String cloudTenantId) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "API key is required");
        }

        CloudTenant cloudTenant = cloudTenantRepository.findById(cloudTenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLOUD_TENANT_NOT_FOUND,
                        "Cloud tenant not found: " + cloudTenantId));

        // Verify API key matches
        if (!passwordEncoder.matches(apiKey, cloudTenant.getApiKeyHash())) {
            log.warn("Invalid API key provided for cloud tenant: {}", cloudTenantId);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid API key");
        }

        // Check if tenant is active
        if (!cloudTenant.isActive()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Cloud tenant is not active");
        }

        return cloudTenant;
    }

    /**
     * Get cloud tenant by ID (for internal use).
     *
     * @param cloudTenantId Cloud tenant ID
     * @return CloudTenant
     */
    public CloudTenant getCloudTenantById(String cloudTenantId) {
        return cloudTenantRepository.findById(cloudTenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLOUD_TENANT_NOT_FOUND,
                        "Cloud tenant not found: " + cloudTenantId));
    }

    /**
     * Get all shops for a cloud tenant.
     *
     * @param cloudTenantId Cloud tenant ID
     * @return List of CloudShop
     */
    public List<CloudShop> getShopsForTenant(String cloudTenantId) {
        return cloudShopRepository.findByCloudTenantId(cloudTenantId);
    }

    /**
     * Create CloudShop entity from DTO.
     *
     * @param cloudTenant Parent cloud tenant
     * @param shopDto Shop registration DTO
     * @return CloudShop
     */
    private CloudShop createCloudShop(CloudTenant cloudTenant, ShopRegistrationDto shopDto) {
        return CloudShop.builder()
                .cloudTenant(cloudTenant)
                .shopName(shopDto.getShopName())
                .shopEmail(shopDto.getShopEmail())
                .address(shopDto.getAddress())
                .city(shopDto.getCity())
                .state(shopDto.getState())
                .country(shopDto.getCountry())
                .phoneNumber(shopDto.getPhoneNumber())
                .status(CloudShop.Status.ACTIVE)
                .build();
    }

    /**
     * Generate a secure random API key.
     *
     * @return API key string
     */
    private String generateApiKey() {
        // Generate UUID-based API key with prefix
        return "rhq_" + UUID.randomUUID().toString().replace("-", "") +
                UUID.randomUUID().toString().replace("-", "");
    }
}