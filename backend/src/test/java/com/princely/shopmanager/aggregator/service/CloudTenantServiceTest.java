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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CloudTenantService.
 * Tests tenant registration, shop linking, API key validation, and unregistration.
 */
@ExtendWith(MockitoExtension.class)
class CloudTenantServiceTest {

    @Mock
    private CloudTenantRepository cloudTenantRepository;

    @Mock
    private CloudShopRepository cloudShopRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CloudTenantService cloudTenantService;

    private TenantRegistrationRequest validRegistrationRequest;
    private CloudTenant savedCloudTenant;
    private static final String TEST_TENANT_EMAIL = "test@retailhq.com";
    private static final String TEST_API_KEY = "rhq_testapikey123456789";
    private static final String TEST_API_KEY_HASH = "$2a$10$hashedApiKey";
    private static final String CLOUD_TENANT_ID = "cloud-tenant-123";

    @BeforeEach
    void setUp() {
        // Setup valid registration request with shops
        ShopRegistrationDto shop1 = ShopRegistrationDto.builder()
                .shopName("Test Shop 1")
                .shopEmail("shop1@test.com")
                .address("123 Main St")
                .city("Test City")
                .country("Test Country")
                .phoneNumber("123-456-7890")
                .build();

        ShopRegistrationDto shop2 = ShopRegistrationDto.builder()
                .shopName("Test Shop 2")
                .shopEmail("shop2@test.com")
                .build();

        validRegistrationRequest = TenantRegistrationRequest.builder()
                .tenantName("Test Retail Business")
                .tenantEmail(TEST_TENANT_EMAIL)
                .companyRegistration("REG123456")
                .taxId("TAX789")
                .address("456 Business Ave")
                .city("Business City")
                .country("Business Country")
                .phoneNumber("987-654-3210")
                .shops(Arrays.asList(shop1, shop2))
                .build();

        // Setup saved cloud tenant
        savedCloudTenant = CloudTenant.builder()
                .id(CLOUD_TENANT_ID)
                .tenantName("Test Retail Business")
                .tenantEmail(TEST_TENANT_EMAIL)
                .apiKeyHash(TEST_API_KEY_HASH)
                .status(CloudTenant.Status.ACTIVE)
                .shopCount(2)
                .subscriptionTier(CloudTenant.SubscriptionTier.FREE)
                .build();
    }

    @Test
    void registerTenant_Success() {
        // Arrange
        when(cloudTenantRepository.existsByTenantEmail(TEST_TENANT_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn(TEST_API_KEY_HASH);
        when(cloudTenantRepository.save(any(CloudTenant.class))).thenReturn(savedCloudTenant);
        when(cloudShopRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // Act
        TenantRegistrationResponse response = cloudTenantService.registerTenant(validRegistrationRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCloudTenantId()).isEqualTo(CLOUD_TENANT_ID);
        assertThat(response.getApiKey()).isNotNull();
        assertThat(response.getApiKey()).startsWith("rhq_");
        assertThat(response.getRegisteredShopsCount()).isEqualTo(2);
        assertThat(response.getMessage()).contains("successfully registered");

        // Verify repository interactions
        verify(cloudTenantRepository).existsByTenantEmail(TEST_TENANT_EMAIL);
        verify(passwordEncoder).encode(anyString());
        verify(cloudTenantRepository).save(any(CloudTenant.class));
        verify(cloudShopRepository).saveAll(anyList());

        // Verify saved cloud tenant properties
        ArgumentCaptor<CloudTenant> tenantCaptor = ArgumentCaptor.forClass(CloudTenant.class);
        verify(cloudTenantRepository).save(tenantCaptor.capture());
        CloudTenant capturedTenant = tenantCaptor.getValue();
        assertThat(capturedTenant.getTenantName()).isEqualTo("Test Retail Business");
        assertThat(capturedTenant.getTenantEmail()).isEqualTo(TEST_TENANT_EMAIL);
        assertThat(capturedTenant.getShopCount()).isEqualTo(2);
        assertThat(capturedTenant.getStatus()).isEqualTo(CloudTenant.Status.ACTIVE);
        assertThat(capturedTenant.getSubscriptionTier()).isEqualTo(CloudTenant.SubscriptionTier.FREE);
    }

    @Test
    void registerTenant_TenantAlreadyExists_ThrowsException() {
        // Arrange
        when(cloudTenantRepository.existsByTenantEmail(TEST_TENANT_EMAIL)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> cloudTenantService.registerTenant(validRegistrationRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BUSINESS_RULE_VIOLATION);

        verify(cloudTenantRepository).existsByTenantEmail(TEST_TENANT_EMAIL);
        verify(cloudTenantRepository, never()).save(any());
        verify(cloudShopRepository, never()).saveAll(anyList());
    }

    @Test
    void registerTenant_NoShopsProvided_ThrowsException() {
        // Arrange
        validRegistrationRequest.setShops(Collections.emptyList());
        when(cloudTenantRepository.existsByTenantEmail(TEST_TENANT_EMAIL)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> cloudTenantService.registerTenant(validRegistrationRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);

        verify(cloudTenantRepository, never()).save(any());
        verify(cloudShopRepository, never()).saveAll(anyList());
    }

    @Test
    void registerTenant_NullShopsList_ThrowsException() {
        // Arrange
        validRegistrationRequest.setShops(null);
        when(cloudTenantRepository.existsByTenantEmail(TEST_TENANT_EMAIL)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> cloudTenantService.registerTenant(validRegistrationRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void linkShop_Success() {
        // Arrange
        ShopRegistrationDto newShop = ShopRegistrationDto.builder()
                .shopName("New Shop")
                .shopEmail("newshop@test.com")
                .build();

        ShopLinkRequest request = ShopLinkRequest.builder()
                .cloudTenantId(CLOUD_TENANT_ID)
                .shop(newShop)
                .build();

        when(cloudTenantRepository.findById(CLOUD_TENANT_ID)).thenReturn(Optional.of(savedCloudTenant));
        when(passwordEncoder.matches(TEST_API_KEY, TEST_API_KEY_HASH)).thenReturn(true);
        when(cloudShopRepository.save(any(CloudShop.class))).thenReturn(new CloudShop());
        when(cloudTenantRepository.save(any(CloudTenant.class))).thenReturn(savedCloudTenant);

        // Act
        cloudTenantService.linkShop(request, TEST_API_KEY);

        // Assert
        verify(cloudTenantRepository).findById(CLOUD_TENANT_ID);
        verify(passwordEncoder).matches(TEST_API_KEY, TEST_API_KEY_HASH);
        verify(cloudShopRepository).save(any(CloudShop.class));
        verify(cloudTenantRepository).save(any(CloudTenant.class));

        // Verify shop count was incremented
        ArgumentCaptor<CloudTenant> tenantCaptor = ArgumentCaptor.forClass(CloudTenant.class);
        verify(cloudTenantRepository).save(tenantCaptor.capture());
        CloudTenant updatedTenant = tenantCaptor.getValue();
        assertThat(updatedTenant.getShopCount()).isEqualTo(3); // Original 2 + 1 new
    }

    @Test
    void linkShop_InvalidApiKey_ThrowsException() {
        // Arrange
        ShopLinkRequest request = ShopLinkRequest.builder()
                .cloudTenantId(CLOUD_TENANT_ID)
                .shop(ShopRegistrationDto.builder().shopName("New Shop").build())
                .build();

        when(cloudTenantRepository.findById(CLOUD_TENANT_ID)).thenReturn(Optional.of(savedCloudTenant));
        when(passwordEncoder.matches(TEST_API_KEY, TEST_API_KEY_HASH)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> cloudTenantService.linkShop(request, TEST_API_KEY))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);

        verify(cloudShopRepository, never()).save(any());
        verify(cloudTenantRepository, never()).save(any());
    }

    @Test
    void linkShop_CloudTenantNotFound_ThrowsException() {
        // Arrange
        ShopLinkRequest request = ShopLinkRequest.builder()
                .cloudTenantId("nonexistent-id")
                .shop(ShopRegistrationDto.builder().shopName("New Shop").build())
                .build();

        when(cloudTenantRepository.findById("nonexistent-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cloudTenantService.linkShop(request, TEST_API_KEY))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLOUD_TENANT_NOT_FOUND);

        verify(cloudShopRepository, never()).save(any());
    }

    @Test
    void unregisterTenant_Success() {
        // Arrange
        CloudShop shop1 = new CloudShop();
        CloudShop shop2 = new CloudShop();
        List<CloudShop> shops = Arrays.asList(shop1, shop2);

        when(cloudTenantRepository.findById(CLOUD_TENANT_ID)).thenReturn(Optional.of(savedCloudTenant));
        when(passwordEncoder.matches(TEST_API_KEY, TEST_API_KEY_HASH)).thenReturn(true);
        when(cloudShopRepository.findByCloudTenantId(CLOUD_TENANT_ID)).thenReturn(shops);

        // Act
        cloudTenantService.unregisterTenant(CLOUD_TENANT_ID, TEST_API_KEY);

        // Assert
        verify(cloudTenantRepository).findById(CLOUD_TENANT_ID);
        verify(passwordEncoder).matches(TEST_API_KEY, TEST_API_KEY_HASH);
        verify(cloudShopRepository).findByCloudTenantId(CLOUD_TENANT_ID);
        verify(cloudShopRepository).deleteAll(shops);
        verify(cloudTenantRepository).delete(savedCloudTenant);
    }

    @Test
    void validateApiKeyAndGetTenant_Success() {
        // Arrange
        when(cloudTenantRepository.findById(CLOUD_TENANT_ID)).thenReturn(Optional.of(savedCloudTenant));
        when(passwordEncoder.matches(TEST_API_KEY, TEST_API_KEY_HASH)).thenReturn(true);

        // Act
        CloudTenant result = cloudTenantService.validateApiKeyAndGetTenant(TEST_API_KEY, CLOUD_TENANT_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(CLOUD_TENANT_ID);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void validateApiKeyAndGetTenant_NullApiKey_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> cloudTenantService.validateApiKeyAndGetTenant(null, CLOUD_TENANT_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);

        verify(cloudTenantRepository, never()).findById(any());
    }

    @Test
    void validateApiKeyAndGetTenant_BlankApiKey_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> cloudTenantService.validateApiKeyAndGetTenant("   ", CLOUD_TENANT_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);
    }

    @Test
    void validateApiKeyAndGetTenant_InvalidApiKey_ThrowsException() {
        // Arrange
        when(cloudTenantRepository.findById(CLOUD_TENANT_ID)).thenReturn(Optional.of(savedCloudTenant));
        when(passwordEncoder.matches("wrong-key", TEST_API_KEY_HASH)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> cloudTenantService.validateApiKeyAndGetTenant("wrong-key", CLOUD_TENANT_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);
    }

    @Test
    void validateApiKeyAndGetTenant_InactiveTenant_ThrowsException() {
        // Arrange
        savedCloudTenant.setStatus(CloudTenant.Status.SUSPENDED);
        when(cloudTenantRepository.findById(CLOUD_TENANT_ID)).thenReturn(Optional.of(savedCloudTenant));
        when(passwordEncoder.matches(TEST_API_KEY, TEST_API_KEY_HASH)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> cloudTenantService.validateApiKeyAndGetTenant(TEST_API_KEY, CLOUD_TENANT_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BUSINESS_RULE_VIOLATION);
    }

    @Test
    void getCloudTenantById_Success() {
        // Arrange
        when(cloudTenantRepository.findById(CLOUD_TENANT_ID)).thenReturn(Optional.of(savedCloudTenant));

        // Act
        CloudTenant result = cloudTenantService.getCloudTenantById(CLOUD_TENANT_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(CLOUD_TENANT_ID);
        verify(cloudTenantRepository).findById(CLOUD_TENANT_ID);
    }

    @Test
    void getCloudTenantById_NotFound_ThrowsException() {
        // Arrange
        when(cloudTenantRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cloudTenantService.getCloudTenantById("nonexistent"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLOUD_TENANT_NOT_FOUND);
    }

    @Test
    void getShopsForTenant_Success() {
        // Arrange
        CloudShop shop1 = new CloudShop();
        CloudShop shop2 = new CloudShop();
        List<CloudShop> shops = Arrays.asList(shop1, shop2);
        when(cloudShopRepository.findByCloudTenantId(CLOUD_TENANT_ID)).thenReturn(shops);

        // Act
        List<CloudShop> result = cloudTenantService.getShopsForTenant(CLOUD_TENANT_ID);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).contains(shop1, shop2);
        verify(cloudShopRepository).findByCloudTenantId(CLOUD_TENANT_ID);
    }
}
