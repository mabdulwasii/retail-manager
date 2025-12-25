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
import org.springframework.web.client.RestTemplate;

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
    private final RestTemplate restTemplate = new RestTemplate();

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
     * Send registration request to cloud
     */
    private TenantRegistrationResponse sendRegistrationRequest(String cloudApiUrl,
            TenantRegistrationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TenantRegistrationRequest> httpRequest = new HttpEntity<>(request, headers);
        String url = cloudApiUrl + "/registration/tenants";

        try {
            ResponseEntity<TenantRegistrationResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpRequest,
                    TenantRegistrationResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }

            throw new BusinessException(ErrorCode.CLOUD_REGISTRATION_FAILED,
                    "Unexpected response from cloud: " + response.getStatusCode());

        } catch (HttpClientErrorException e) {
            log.error("Cloud registration failed with client error: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.CLOUD_REGISTRATION_FAILED,
                    "Cloud registration failed: " + e.getStatusCode());
        } catch (HttpServerErrorException e) {
            log.error("Cloud registration failed with server error: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.CLOUD_SYNC_UNAVAILABLE,
                    "Cloud service unavailable");
        } catch (Exception e) {
            log.error("Cloud registration failed: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.CLOUD_REGISTRATION_FAILED,
                    "Cloud registration failed: " + e.getMessage());
        }
    }

    /**
     * Send shop link request to cloud
     */
    private void sendShopLinkRequest(String cloudApiUrl, String apiKey, ShopLinkRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", apiKey);

        HttpEntity<ShopLinkRequest> httpRequest = new HttpEntity<>(request, headers);
        String url = cloudApiUrl + "/registration/shops";

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpRequest,
                    Void.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new BusinessException(ErrorCode.CLOUD_REGISTRATION_FAILED,
                        "Shop link failed: " + response.getStatusCode());
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Shop link failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.CLOUD_REGISTRATION_FAILED,
                    "Shop link failed: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("Shop link failed: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.CLOUD_REGISTRATION_FAILED,
                    "Shop link failed: " + e.getMessage());
        }
    }

    /**
     * Send unregister request to cloud
     */
    private void sendUnregisterRequest(String cloudApiUrl, String apiKey, String cloudTenantId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", apiKey);

        HttpEntity<Void> httpRequest = new HttpEntity<>(headers);
        String url = cloudApiUrl + "/registration/tenants/" + cloudTenantId;

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, httpRequest, Void.class);
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
