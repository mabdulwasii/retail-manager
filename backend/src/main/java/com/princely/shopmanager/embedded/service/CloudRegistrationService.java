package com.princely.shopmanager.embedded.service;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.embedded.domain.CloudSyncConfig;
import com.princely.shopmanager.embedded.repository.CloudSyncConfigRepository;
import com.princely.shopmanager.shared.exception.BusinessException;
import com.princely.shopmanager.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Service for registering local embedded tenants with cloud aggregator.
 * Handles initial registration and linking of tenant + shops.
 */
@Service
@Profile("embedded")
@RequiredArgsConstructor
@Slf4j
public class CloudRegistrationService {

    private final TenantRepository tenantRepository;
    private final ShopRepository shopRepository;
    private final CloudSyncConfigRepository cloudSyncConfigRepository;
    private final CloudSyncConfigurationService cloudSyncConfigurationService;
    private final RestClient restClient;

    @Value("${application.cloud.registration-url:https://cloud.shopmanager.com/api}")
    private String cloudRegistrationUrl;

    /**
     * Register tenant with cloud aggregator
     */
    @Transactional
    public CloudSyncConfig registerTenant(String tenantId, String cloudApiUrl) {
        log.info("Registering tenant {} with cloud aggregator", tenantId);

        // Check if already registered
        if (cloudSyncConfigRepository.existsByTenantId(tenantId)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Tenant is already registered with cloud aggregator");
        }

        // Get tenant details
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));

        // Get tenant's shops
        List<Shop> shops = shopRepository.findByTenantId(tenantId);

        // Prepare registration request
        TenantRegistrationRequest request = TenantRegistrationRequest.builder()
                .tenantName(tenant.getName())
                .tenantEmail(tenant.getContactEmail())
                .companyRegistration(tenant.getCompanyRegistration())
                .taxId(tenant.getTaxId())
                .address(tenant.getPrimaryAddress())
                .city(tenant.getCity())
                .country(tenant.getCountry())
                .shops(shops.stream()
                        .map(shop -> ShopRegistrationDto.builder()
                                .shopName(shop.getName())
                                .shopEmail(shop.getEmail())
                                .address(shop.getAddress())
                                .city(shop.getCity())
                                .country(shop.getCountry())
                                .phoneNumber(shop.getPhoneNumber())
                                .build())
                        .toList())
                .build();

        // Send registration request to cloud
        TenantRegistrationResponse response = sendRegistrationRequest(cloudApiUrl, request);

        // Create cloud sync configuration
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(tenantId)
                .cloudTenantId(response.cloudTenantId())
                .cloudApiKey(response.apiKey())
                .cloudApiUrl(cloudApiUrl)
                .syncEnabled(true) // Enable by default
                .syncStatus(CloudSyncConfig.SyncStatus.CONFIGURED)
                .build();

        CloudSyncConfig saved = cloudSyncConfigurationService.saveConfiguration(config);
        log.info("Tenant {} successfully registered with cloud. Cloud Tenant ID: {}",
                tenantId, response.cloudTenantId());

        return saved;
    }

    /**
     * Link additional shop to existing cloud tenant
     */
    @Transactional
    public void linkShop(String tenantId, String shopId) {
        log.info("Linking shop {} to cloud tenant", shopId);

        // Get cloud sync config
        CloudSyncConfig config = cloudSyncConfigurationService.getConfigByTenantIdOrThrow(tenantId);

        if (!config.isConfigured()) {
            throw new BusinessException(ErrorCode.CLOUD_SYNC_NOT_CONFIGURED);
        }

        // Get shop details
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_NOT_FOUND));

        // Verify shop belongs to tenant
        if (!shop.getTenant().getId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.SHOP_ACCESS_DENIED,
                    "Shop does not belong to the specified tenant");
        }

        // Prepare shop link request
        ShopLinkRequest request = ShopLinkRequest.builder()
                .cloudTenantId(config.getCloudTenantId())
                .shop(ShopRegistrationDto.builder()
                        .shopName(shop.getName())
                        .shopEmail(shop.getEmail())
                        .address(shop.getAddress())
                        .city(shop.getCity())
                        .country(shop.getCountry())
                        .phoneNumber(shop.getPhoneNumber())
                        .build())
                .build();

        // Send shop link request to cloud
        sendShopLinkRequest(config.getCloudApiUrl(), config.getCloudApiKey(), request);

        log.info("Shop {} successfully linked to cloud tenant {}", shopId, config.getCloudTenantId());
    }

    /**
     * Unregister tenant from cloud
     */
    @Transactional
    public void unregisterTenant(String tenantId) {
        log.info("Unregistering tenant {} from cloud", tenantId);

        CloudSyncConfig config = cloudSyncConfigurationService.getConfigByTenantIdOrThrow(tenantId);

        // Send unregister request to cloud
        try {
            sendUnregisterRequest(config.getCloudApiUrl(), config.getCloudApiKey(),
                    config.getCloudTenantId());
        } catch (Exception e) {
            log.warn("Failed to unregister from cloud (proceeding with local deletion): {}",
                    e.getMessage());
        }

        // Delete local cloud sync config
        cloudSyncConfigurationService.deleteConfiguration(tenantId);

        log.info("Tenant {} unregistered from cloud", tenantId);
    }

    /**
     * Send registration request to cloud using RestClient
     */
    private TenantRegistrationResponse sendRegistrationRequest(String cloudApiUrl,
            TenantRegistrationRequest request) {
        String url = cloudApiUrl + "/registration/tenants";

        try {
            TenantRegistrationResponse response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                        log.error("Cloud registration failed with client error: {}", resp.getStatusCode());
                        throw new BusinessException(ErrorCode.CLOUD_REGISTRATION_FAILED,
                                "Cloud registration failed: " + resp.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                        log.error("Cloud registration failed with server error: {}", resp.getStatusCode());
                        throw new BusinessException(ErrorCode.CLOUD_SYNC_UNAVAILABLE,
                                "Cloud service unavailable");
                    })
                    .body(TenantRegistrationResponse.class);

            if (response != null) {
                return response;
            }

            throw new BusinessException(ErrorCode.CLOUD_REGISTRATION_FAILED,
                    "Unexpected null response from cloud");

        } catch (BusinessException e) {
            throw e; // Re-throw BusinessException
        } catch (Exception e) {
            log.error("Cloud registration failed: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.CLOUD_REGISTRATION_FAILED,
                    "Cloud registration failed: " + e.getMessage());
        }
    }

    /**
     * Send shop link request to cloud using RestClient
     */
    private void sendShopLinkRequest(String cloudApiUrl, String apiKey, ShopLinkRequest request) {
        String url = cloudApiUrl + "/registration/shops";

        try {
            restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-API-Key", apiKey)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                        log.error("Shop link failed with client error: {}", resp.getStatusCode());
                        throw new BusinessException(ErrorCode.CLOUD_REGISTRATION_FAILED,
                                "Shop link failed: " + resp.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                        log.error("Shop link failed with server error: {}", resp.getStatusCode());
                        throw new BusinessException(ErrorCode.CLOUD_SYNC_UNAVAILABLE,
                                "Cloud service unavailable during shop link");
                    })
                    .toBodilessEntity();

        } catch (BusinessException e) {
            throw e; // Re-throw BusinessException
        } catch (Exception e) {
            log.error("Shop link failed: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.CLOUD_REGISTRATION_FAILED,
                    "Shop link failed: " + e.getMessage());
        }
    }

    /**
     * Send unregister request to cloud using RestClient
     */
    private void sendUnregisterRequest(String cloudApiUrl, String apiKey, String cloudTenantId) {
        String url = cloudApiUrl + "/registration/tenants/" + cloudTenantId;

        try {
            restClient.delete()
                    .uri(url)
                    .header("X-API-Key", apiKey)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to unregister tenant from cloud: {}", e.getMessage());
            throw e;
        }
    }

    // DTO classes
    @lombok.Builder
    @lombok.Data
    public static class TenantRegistrationRequest {
        private String tenantName;
        private String tenantEmail;
        private String companyRegistration;
        private String taxId;
        private String address;
        private String city;
        private String country;
        private List<ShopRegistrationDto> shops;
    }

    @lombok.Builder
    @lombok.Data
    public static class ShopRegistrationDto {
        private String shopName;
        private String shopEmail;
        private String address;
        private String city;
        private String country;
        private String phoneNumber;
    }

    @lombok.Builder
    @lombok.Data
    public static class ShopLinkRequest {
        private String cloudTenantId;
        private ShopRegistrationDto shop;
    }

    public record TenantRegistrationResponse(String cloudTenantId, String apiKey, String message) {
    }
}
