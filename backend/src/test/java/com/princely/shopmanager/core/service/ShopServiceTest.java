package com.princely.shopmanager.core.service;

import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.dto.ShopCreateRequest;
import com.princely.shopmanager.core.dto.ShopResponse;
import com.princely.shopmanager.core.dto.ShopUpdateRequest;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.shared.service.AuditService;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private ShopStatusStateMachine stateMachine;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ShopService shopService;

    private Shop testShop;
    private Tenant testTenant;
    private ShopCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testTenant = new Tenant();
        testTenant.setId("tenant-test-shop");
        testTenant.setName("Test Shop Organization");
        testTenant.setContactEmail("admin@testshop.com");
        testTenant.setStatus(Tenant.TenantStatus.ACTIVE);

        testShop = new Shop();
        testShop.setId("shop-1");
        testShop.setName("Test Shop");
        testShop.setTenant(testTenant);
        testShop.setStatus(Shop.ShopStatus.ACTIVE);
        testShop.setDescription("A test shop");
        testShop.setAddress("123 Test St");
        testShop.setCity("Test City");

        createRequest = new ShopCreateRequest();
        createRequest.setName("New Shop");
        createRequest.setDescription("A new shop");
        createRequest.setAddress("456 New St");
        createRequest.setCity("New City");
        createRequest.setState("Test State");
        createRequest.setCountry("Test Country");
        createRequest.setPostalCode("12345");
        createRequest.setPhoneNumber("555-0123");
        createRequest.setEmail("contact@newshop.com");
    }

    @Test
    void createShop_ShouldCreateShopWithNewTenant() {
        // Arrange
        User mockContactUser = User.builder()
            .id("user-1")
            .tenant(testTenant)
            .email("admin@testshop.com")
            .username("admin-testshop")
            .firstName("Admin")
            .lastName("User")
            .status(User.UserStatus.ACTIVE)
            .build();

        when(shopRepository.findByName("New Shop")).thenReturn(Optional.empty());
        when(tenantRepository.existsById(anyString())).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);
        when(userRepository.save(any(User.class))).thenReturn(mockContactUser);
        when(shopRepository.save(any(Shop.class))).thenReturn(testShop);

        // Act
        ShopResponse result = shopService.createShop(createRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Shop");

        ArgumentCaptor<Tenant> tenantCaptor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository, times(2)).save(tenantCaptor.capture());
        List<Tenant> savedTenants = tenantCaptor.getAllValues();
        assertThat(savedTenants.get(0).getName()).isEqualTo("New Shop Organization");
        assertThat(savedTenants.get(0).getId()).startsWith("tenant-new-shop");

        verify(userRepository).save(any(User.class));
        verify(shopRepository).save(any(Shop.class));
        verify(auditService).logEntityCreation(eq("Shop"), anyString(), anyString());
    }

    @Test
    void createShop_WithExistingShopName_ShouldThrowException() {
        // Arrange
        when(shopRepository.findByName("New Shop")).thenReturn(Optional.of(testShop));

        // Act & Assert
        assertThatThrownBy(() -> shopService.createShop(createRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Shop with name 'New Shop' already exists");

        verify(tenantRepository, never()).save(any());
        verify(shopRepository, never()).save(any());
    }

    @Test
    void getShop_WithValidIdAndAccess_ShouldReturnShop() {
        // Arrange
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));

        try (var mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn("tenant-test-shop");

            // Act
            ShopResponse result = shopService.getShop("shop-1");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("shop-1");
            assertThat(result.getName()).isEqualTo("Test Shop");
        }
    }

    @Test
    void getShop_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(shopRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> shopService.getShop("invalid-id"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Shop not found: invalid-id");
    }

    @Test
    void getShop_WithAccessDenied_ShouldThrowException() {
        // Arrange
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));

        try (var mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn("different-tenant");

            // Act & Assert
            assertThatThrownBy(() -> shopService.getShop("shop-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Access denied to shop: shop-1");
        }
    }

    @Test
    void getShops_AsSystemAdmin_ShouldReturnAllShops() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Shop> mockPage = new PageImpl<>(List.of(testShop));
        when(shopRepository.findAll(pageable)).thenReturn(mockPage);

        try (var mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn(null);

            // Act
            Page<ShopResponse> result = shopService.getShops(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo("shop-1");

            verify(shopRepository).findAll(pageable);
            verify(shopRepository, never()).findByTenant_Id(anyString(), any());
        }
    }

    @Test
    void getShops_AsTenantUser_ShouldReturnTenantShops() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Shop> mockPage = new PageImpl<>(List.of(testShop));
        when(shopRepository.findByTenant_Id("tenant-test-shop", pageable)).thenReturn(mockPage);

        try (var mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn("tenant-test-shop");

            // Act
            Page<ShopResponse> result = shopService.getShops(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            verify(shopRepository).findByTenant_Id("tenant-test-shop", pageable);
            verify(shopRepository, never()).findAll(any(Pageable.class));
        }
    }

    @Test
    void getActiveShops_AsSystemAdmin_ShouldReturnAllActiveShops() {
        // Arrange
        List<Shop> activeShops = Arrays.asList(testShop);
        when(shopRepository.findByStatus(Shop.ShopStatus.ACTIVE)).thenReturn(activeShops);

        try (var mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn(null);

            // Act
            List<ShopResponse> result = shopService.getActiveShops();

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo("shop-1");
            assertThat(result.get(0).getStatus()).isEqualTo("ACTIVE");

            verify(shopRepository).findByStatus(Shop.ShopStatus.ACTIVE);
        }
    }

    @Test
    void getActiveShops_AsTenantUser_ShouldReturnTenantActiveShops() {
        // Arrange
        List<Shop> activeShops = Arrays.asList(testShop);
        when(shopRepository.findByTenant_IdAndStatus("tenant-test-shop", Shop.ShopStatus.ACTIVE))
            .thenReturn(activeShops);

        try (var mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn("tenant-test-shop");

            // Act
            List<ShopResponse> result = shopService.getActiveShops();

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo("shop-1");

            verify(shopRepository).findByTenant_IdAndStatus("tenant-test-shop", Shop.ShopStatus.ACTIVE);
        }
    }

    @Test
    void updateShop_WithValidRequest_ShouldUpdateShop() {
        // Arrange
        ShopUpdateRequest updateRequest = new ShopUpdateRequest();
        updateRequest.setDescription("Updated description");

        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(shopRepository.save(any(Shop.class))).thenReturn(testShop);

        try (var mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn("tenant-test-shop");

            // Act
            ShopResponse result = shopService.updateShop("shop-1", updateRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("shop-1");

            verify(shopRepository).save(testShop);
        }
    }

    @Test
    void changeShopStatus_WithValidTransition_ShouldChangeStatus() {
        // Arrange
        testShop.setStatus(Shop.ShopStatus.ACTIVE);
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(shopRepository.save(any(Shop.class))).thenReturn(testShop);
        doNothing().when(stateMachine).validateTransition(Shop.ShopStatus.ACTIVE, Shop.ShopStatus.SUSPENDED, "shop-1");

        try (var mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn("tenant-test-shop");

            // Act
            ShopResponse result = shopService.changeShopStatus("shop-1", Shop.ShopStatus.SUSPENDED);

            // Assert
            assertThat(result).isNotNull();

            verify(shopRepository).save(testShop);
            verify(auditService).logEntityModification(eq("Shop"), eq("shop-1"), anyString());
        }
    }

    @Test
    void changeShopStatus_WithInvalidTransition_ShouldThrowException() {
        // Arrange
        testShop.setStatus(Shop.ShopStatus.CLOSED);
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        doThrow(new IllegalArgumentException("Cannot change status of closed shop"))
            .when(stateMachine).validateTransition(Shop.ShopStatus.CLOSED, Shop.ShopStatus.ACTIVE, "shop-1");

        try (var mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn("tenant-test-shop");

            // Act & Assert
            assertThatThrownBy(() -> shopService.changeShopStatus("shop-1", Shop.ShopStatus.ACTIVE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot change status of closed shop");

            verify(shopRepository, never()).save(any());
        }
    }

    @Test
    void deleteShop_ShouldSoftDeleteShop() {
        // Arrange
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(shopRepository.save(any(Shop.class))).thenReturn(testShop);

        try (var mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn("tenant-test-shop");

            // Act
            shopService.deleteShop("shop-1");

            // Assert
            ArgumentCaptor<Shop> shopCaptor = ArgumentCaptor.forClass(Shop.class);
            verify(shopRepository).save(shopCaptor.capture());

            Shop savedShop = shopCaptor.getValue();
            assertThat(savedShop.getStatus()).isEqualTo(Shop.ShopStatus.CLOSED);

            verify(auditService).logEntityDeletion(eq("Shop"), eq("shop-1"), anyString());
        }
    }

    @Test
    void deleteShop_WithAccessDenied_ShouldThrowException() {
        // Arrange
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));

        try (var mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn("different-tenant");

            // Act & Assert
            assertThatThrownBy(() -> shopService.deleteShop("shop-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Access denied to shop: shop-1");

            verify(shopRepository, never()).save(any());
        }
    }
}