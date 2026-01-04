package com.princely.shopmanager.embedded.service;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.embedded.domain.CloudSyncConfig;
import com.princely.shopmanager.embedded.repository.CloudSyncConfigRepository;
import com.princely.shopmanager.shared.exception.BusinessException;
import com.princely.shopmanager.shared.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Cloud Registration Service Tests")
class CloudRegistrationServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private CloudSyncConfigRepository cloudSyncConfigRepository;

    @Mock
    private CloudSyncConfigurationService cloudSyncConfigurationService;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @InjectMocks
    private CloudRegistrationService cloudRegistrationService;

    private static final String TEST_TENANT_ID = "tenant-123";
    private static final String TEST_SHOP_ID = "shop-456";
    private static final String TEST_CLOUD_URL = "https://cloud.test.com/api";
    private static final String TEST_CLOUD_TENANT_ID = "cloud-tenant-789";
    private static final String TEST_API_KEY = "test-api-key-xyz";

    private Tenant testTenant;
    private Shop testShop;
    private CloudSyncConfig testConfig;

    @BeforeEach
    void setUp() {
        // Set cloud registration URL
        ReflectionTestUtils.setField(cloudRegistrationService, "cloudRegistrationUrl", TEST_CLOUD_URL);

        // Setup test tenant
        testTenant = new Tenant();
        testTenant.setId(TEST_TENANT_ID);
        testTenant.setName("Test Tenant");
        testTenant.setContactEmail("tenant@test.com");
        testTenant.setCompanyRegistration("REG123");
        testTenant.setTaxId("TAX456");
        testTenant.setPrimaryAddress("123 Main St");
        testTenant.setCity("Test City");
        testTenant.setCountry("Test Country");

        // Setup test shop
        testShop = new Shop();
        testShop.setId(TEST_SHOP_ID);
        testShop.setName("Test Shop");
        testShop.setEmail("shop@test.com");
        testShop.setAddress("456 Shop St");
        testShop.setCity("Shop City");
        testShop.setCountry("Shop Country");
        testShop.setPhoneNumber("+1234567890");
        testShop.setTenant(testTenant);

        // Setup test cloud sync config
        testConfig = CloudSyncConfig.builder()
                .tenantId(TEST_TENANT_ID)
                .cloudTenantId(TEST_CLOUD_TENANT_ID)
                .cloudApiKey(TEST_API_KEY)
                .cloudApiUrl(TEST_CLOUD_URL)
                .syncEnabled(true)
                .syncStatus(CloudSyncConfig.SyncStatus.CONFIGURED)
                .build();
    }

    // ========== registerTenant() Tests ==========

    @Test
    @DisplayName("Should throw exception when tenant already registered")
    void shouldThrowExceptionWhenTenantAlreadyRegistered() {
        // Given
        when(cloudSyncConfigRepository.existsByTenantId(TEST_TENANT_ID)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> cloudRegistrationService.registerTenant(TEST_TENANT_ID, TEST_CLOUD_URL))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BUSINESS_RULE_VIOLATION);

        verify(cloudSyncConfigRepository).existsByTenantId(TEST_TENANT_ID);
        verifyNoInteractions(tenantRepository, shopRepository, cloudSyncConfigurationService);
    }

    @Test
    @DisplayName("Should throw exception when tenant not found")
    void shouldThrowExceptionWhenTenantNotFound() {
        // Given
        when(cloudSyncConfigRepository.existsByTenantId(TEST_TENANT_ID)).thenReturn(false);
        when(tenantRepository.findById(TEST_TENANT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cloudRegistrationService.registerTenant(TEST_TENANT_ID, TEST_CLOUD_URL))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TENANT_NOT_FOUND);

        verify(cloudSyncConfigRepository).existsByTenantId(TEST_TENANT_ID);
        verify(tenantRepository).findById(TEST_TENANT_ID);
        verifyNoInteractions(shopRepository, cloudSyncConfigurationService);
    }

    // ========== linkShop() Tests ==========

    @Test
    @DisplayName("Should throw exception when cloud sync not configured for shop linking")
    void shouldThrowExceptionWhenCloudSyncNotConfiguredForShopLink() {
        // Given
        CloudSyncConfig unconfiguredConfig = CloudSyncConfig.builder()
                .tenantId(TEST_TENANT_ID)
                .syncStatus(CloudSyncConfig.SyncStatus.NOT_CONFIGURED)
                .build();

        when(cloudSyncConfigurationService.getConfigByTenantIdOrThrow(TEST_TENANT_ID))
                .thenReturn(unconfiguredConfig);

        // When & Then
        assertThatThrownBy(() -> cloudRegistrationService.linkShop(TEST_TENANT_ID, TEST_SHOP_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLOUD_SYNC_NOT_CONFIGURED);

        verify(cloudSyncConfigurationService).getConfigByTenantIdOrThrow(TEST_TENANT_ID);
        verifyNoInteractions(shopRepository);
    }

    @Test
    @DisplayName("Should throw exception when shop not found for linking")
    void shouldThrowExceptionWhenShopNotFoundForLinking() {
        // Given
        when(cloudSyncConfigurationService.getConfigByTenantIdOrThrow(TEST_TENANT_ID))
                .thenReturn(testConfig);
        when(shopRepository.findById(TEST_SHOP_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cloudRegistrationService.linkShop(TEST_TENANT_ID, TEST_SHOP_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHOP_NOT_FOUND);

        verify(cloudSyncConfigurationService).getConfigByTenantIdOrThrow(TEST_TENANT_ID);
        verify(shopRepository).findById(TEST_SHOP_ID);
    }

    @Test
    @DisplayName("Should throw exception when shop doesn't belong to tenant")
    void shouldThrowExceptionWhenShopDoesNotBelongToTenant() {
        // Given
        Tenant otherTenant = new Tenant();
        otherTenant.setId("other-tenant");
        testShop.setTenant(otherTenant);

        when(cloudSyncConfigurationService.getConfigByTenantIdOrThrow(TEST_TENANT_ID))
                .thenReturn(testConfig);
        when(shopRepository.findById(TEST_SHOP_ID)).thenReturn(Optional.of(testShop));

        // When & Then
        assertThatThrownBy(() -> cloudRegistrationService.linkShop(TEST_TENANT_ID, TEST_SHOP_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHOP_ACCESS_DENIED);

        verify(cloudSyncConfigurationService).getConfigByTenantIdOrThrow(TEST_TENANT_ID);
        verify(shopRepository).findById(TEST_SHOP_ID);
    }

    // ========== unregisterTenant() Tests ==========

    @Test
    @DisplayName("Should throw exception when tenant not found for unregistration")
    void shouldThrowExceptionWhenTenantNotFoundForUnregistration() {
        // Given
        when(cloudSyncConfigurationService.getConfigByTenantIdOrThrow(TEST_TENANT_ID))
                .thenThrow(new BusinessException(ErrorCode.CLOUD_SYNC_NOT_CONFIGURED));

        // When & Then
        assertThatThrownBy(() -> cloudRegistrationService.unregisterTenant(TEST_TENANT_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLOUD_SYNC_NOT_CONFIGURED);

        verify(cloudSyncConfigurationService).getConfigByTenantIdOrThrow(TEST_TENANT_ID);
        verify(cloudSyncConfigurationService, never()).deleteConfiguration(anyString());
    }
}
