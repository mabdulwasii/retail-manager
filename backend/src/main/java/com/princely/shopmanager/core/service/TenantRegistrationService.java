package com.princely.shopmanager.core.service;

import com.princely.shopmanager.auth.service.CreateKeycloakUserRequest;
import com.princely.shopmanager.auth.service.KeycloakUserService;
import com.princely.shopmanager.core.domain.*;
import com.princely.shopmanager.core.dto.registration.*;
import com.princely.shopmanager.core.event.TenantRegistrationEvent;
import com.princely.shopmanager.core.event.TenantRegistrationNotificationEvent;
import com.princely.shopmanager.core.event.TenantActivationNotificationEvent;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.shared.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for handling tenant registration workflow
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantRegistrationService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final RoleRepository roleRepository;
    private final KeycloakUserService keycloakUserService;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;

    /**
     * Process complete tenant registration
     */
    @Transactional
    public TenantRegistrationResponse registerTenant(TenantRegistrationRequest request, String clientIp, String userAgent) {
        log.info("Processing tenant registration for: {}", request.getTenantInfo().name());

        try {
            // Validate registration data
            validateRegistrationRequest(request);

            // Generate password for contact user
            String generatedPassword = keycloakUserService.generatePassword();

            // Create tenant entity (INACTIVE by default)
            Tenant tenant = createTenantEntity(request.getTenantInfo());

            // Create contact user entity (INACTIVE by default)
            User contactUser = createContactUserEntity(request.getContactUser(), tenant);

            // Set contact user on tenant
            tenant.setContactUser(contactUser);
            tenant = tenantRepository.save(tenant);

            // Create user in Keycloak (DISABLED by default)
            String keycloakId = createKeycloakUser(request.getContactUser(), tenant.getId(), generatedPassword);
            contactUser.setKeycloakId(keycloakId);
            contactUser = userRepository.save(contactUser);

            // Create shops (INACTIVE by default)
            List<Shop> shops = createShopEntities(request.getShops(), tenant);
            List<String> shopIds = shops.stream().map(Shop::getId).collect(Collectors.toList());

            // Create audit log
            auditService.logEvent("TENANT_REGISTRATION",
                "Tenant registration submitted: " + tenant.getName(),
                Map.of("tenantId", tenant.getId(), "contactUserId", contactUser.getId(), "shopCount", shops.size()));

            // Publish registration event
            publishRegistrationEvent(tenant, contactUser, shopIds, clientIp, userAgent, request);

            // Publish notification event
            publishNotificationEvent(tenant, contactUser, clientIp, userAgent);

            log.info("Tenant registration completed successfully: {}", tenant.getId());

            return TenantRegistrationResponse.success(
                tenant.getId(),
                tenant.getName(),
                contactUser.getId(),
                contactUser.getEmail(),
                shopIds
            );

        } catch (Exception e) {
            log.error("Failed to process tenant registration", e);
            throw new TenantRegistrationException("Registration failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get all pending tenant registrations for admin review
     */
    @Transactional(readOnly = true)
    public List<PendingTenantResponse> getPendingRegistrations() {
        log.info("Retrieving pending tenant registrations");

        List<Tenant> pendingTenants = tenantRepository.findByStatus(Tenant.TenantStatus.INACTIVE);

        return pendingTenants.stream()
            .map(this::mapToPendingTenantResponse)
            .collect(Collectors.toList());
    }

    /**
     * Activate a tenant registration (admin action)
     */
    @Transactional
    public void activateTenant(TenantActivationRequest request, String adminUserId) {
        log.info("Processing tenant activation: {} by admin: {}", request.tenantId(), adminUserId);

        Tenant tenant = tenantRepository.findById(request.tenantId())
            .orElseThrow(() -> new TenantRegistrationException("Tenant not found: " + request.tenantId()));

        if (request.approved()) {
            // Activate tenant
            tenant.setStatus(Tenant.TenantStatus.ACTIVE);
            tenantRepository.save(tenant);

            // Activate contact user
            User contactUser = tenant.getContactUser();
            if (contactUser != null) {
                contactUser.setStatus(User.UserStatus.ACTIVE);
                userRepository.save(contactUser);

                // Enable user in Keycloak
                keycloakUserService.updateUserStatus(contactUser.getKeycloakId(), true);
            }

            // Activate specified shops
            if (request.shopIdsToActivate() != null) {
                List<Shop> shopsToActivate = shopRepository.findAllById(request.shopIdsToActivate());
                shopsToActivate.forEach(shop -> {
                    shop.setStatus(Shop.ShopStatus.ACTIVE);
                    shop.setOpeningDate(LocalDateTime.now());
                });
                shopRepository.saveAll(shopsToActivate);
            }

            // Create audit log
            auditService.logEvent("TENANT_ACTIVATION",
                "Tenant activated by admin: " + tenant.getName(),
                Map.of("tenantId", tenant.getId(), "adminUserId", adminUserId, "activatedShops", request.shopIdsToActivate().size()));

            // Publish activation notification event
            publishActivationNotificationEvent(tenant, true, null, adminUserId);

            log.info("Tenant activated successfully: {}", tenant.getId());

        } else {
            // Reject tenant - mark as TERMINATED
            tenant.setStatus(Tenant.TenantStatus.TERMINATED);
            tenantRepository.save(tenant);

            // Disable user in Keycloak
            User contactUser = tenant.getContactUser();
            if (contactUser != null && contactUser.getKeycloakId() != null) {
                keycloakUserService.updateUserStatus(contactUser.getKeycloakId(), false);
            }

            // Create audit log for rejection
            auditService.logEvent("TENANT_REJECTION",
                "Tenant registration rejected: " + tenant.getName(),
                Map.of("tenantId", tenant.getId(), "adminUserId", adminUserId, "reason", request.rejectionReason()));

            // Publish rejection notification event
            publishActivationNotificationEvent(tenant, false, request.rejectionReason(), adminUserId);

            log.info("Tenant registration rejected: {}", tenant.getId());
        }
    }

    private void validateRegistrationRequest(TenantRegistrationRequest request) {
        // Check if tenant name already exists
        if (tenantRepository.existsByNameIgnoreCase(request.getTenantInfo().name())) {
            throw new TenantRegistrationException("Tenant name already exists: " + request.getTenantInfo().name());
        }

        // Check if contact user email already exists
        if (userRepository.existsByEmailIgnoreCase(request.getContactUser().email())) {
            throw new TenantRegistrationException("User with email already exists: " + request.getContactUser().email());
        }

        // Check if contact user username already exists
        if (userRepository.existsByUsernameIgnoreCase(request.getContactUser().username())) {
            throw new TenantRegistrationException("Username already exists: " + request.getContactUser().username());
        }

        // Check Keycloak for existing users
        if (keycloakUserService.userExistsByEmail(request.getContactUser().email())) {
            throw new TenantRegistrationException("User already exists in authentication system: " + request.getContactUser().email());
        }

        if (keycloakUserService.userExistsByUsername(request.getContactUser().username())) {
            throw new TenantRegistrationException("Username already exists in authentication system: " + request.getContactUser().username());
        }

        // Validate terms acceptance
        if (!request.isTermsAccepted() || !request.isPrivacyPolicyAccepted()) {
            throw new TenantRegistrationException("Terms and conditions must be accepted");
        }
    }

    private Tenant createTenantEntity(TenantInfoRequest tenantInfo) {
        return Tenant.builder()
            .name(tenantInfo.name())
            .description(tenantInfo.description())
            .contactEmail(tenantInfo.email())
            .primaryAddress(tenantInfo.primaryAddress())
            .city(tenantInfo.city())
            .state(tenantInfo.state())
            .country(tenantInfo.country())
            .postalCode(tenantInfo.postalCode())
            .companyRegistration(tenantInfo.companyRegistration())
            .taxId(tenantInfo.taxId())
            .contactPhone(tenantInfo.contactPhone())
            .status(Tenant.TenantStatus.INACTIVE)
            .createdDate(LocalDateTime.now())
            .build();
    }

    private User createContactUserEntity(ContactUserRequest contactUser, Tenant tenant) {
        // Get TENANT_ADMIN role
        Role tenantAdminRole = roleRepository.findByName("TENANT_ADMIN")
            .orElseThrow(() -> new TenantRegistrationException("TENANT_ADMIN role not found"));

        return User.builder()
            .tenant(tenant)
            .username(contactUser.username())
            .email(contactUser.email())
            .firstName(contactUser.firstName())
            .lastName(contactUser.lastName())
            .phoneNumber(contactUser.phoneNumber())
            .status(User.UserStatus.INACTIVE)
            .roles(Set.of(tenantAdminRole))
            .build();
    }

    private List<Shop> createShopEntities(List<ShopInfoRequest> shopRequests, Tenant tenant) {
        List<Shop> shops = new ArrayList<>();

        for (ShopInfoRequest shopRequest : shopRequests) {
            Shop shop = Shop.builder()
                .tenant(tenant)
                .name(shopRequest.name())
                .description(shopRequest.description())
                .address(shopRequest.address())
                .city(shopRequest.city())
                .state(shopRequest.state())
                .country(shopRequest.country())
                .postalCode(shopRequest.postalCode())
                .phoneNumber(shopRequest.phoneNumber())
                .email(shopRequest.email())
                .taxId(shopRequest.taxId())
                .status(Shop.ShopStatus.INACTIVE)
                .build();

            shops.add(shopRepository.save(shop));
        }

        return shops;
    }

    private String createKeycloakUser(ContactUserRequest contactUser, String tenantId, String password) {
        CreateKeycloakUserRequest keycloakRequest = CreateKeycloakUserRequest.forTenantAdmin(
            contactUser.username(),
            contactUser.email(),
            contactUser.firstName(),
            contactUser.lastName(),
            contactUser.phoneNumber(),
            tenantId,
            password
        );

        return keycloakUserService.createUser(keycloakRequest);
    }

    private void publishRegistrationEvent(Tenant tenant, User contactUser, List<String> shopIds,
                                        String clientIp, String userAgent, TenantRegistrationRequest request) {
        Map<String, Object> metadata = Map.of(
            "registrationData", request,
            "generatedPassword", "REDACTED",
            "timestamp", LocalDateTime.now()
        );

        TenantRegistrationEvent event = new TenantRegistrationEvent(
            this,
            tenant.getId(),
            tenant.getName(),
            contactUser.getId(),
            contactUser.getEmail(),
            shopIds,
            clientIp,
            userAgent,
            metadata
        );

        eventPublisher.publishEvent(event);
    }

    /**
     * Get detailed information about a specific tenant registration
     */
    @Transactional(readOnly = true)
    public PendingTenantResponse getTenantDetails(String tenantId) {
        log.info("Retrieving tenant details for: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new TenantRegistrationException("Tenant not found: " + tenantId));

        return mapToPendingTenantResponse(tenant);
    }

    private PendingTenantResponse mapToPendingTenantResponse(Tenant tenant) {
        List<PendingTenantResponse.PendingShopInfo> shopInfos = tenant.getShops().stream()
            .map(shop -> new PendingTenantResponse.PendingShopInfo(
                shop.getId(),
                shop.getName(),
                shop.getDescription(),
                shop.getAddress(),
                shop.getCity(),
                shop.getState(),
                shop.getCountry(),
                shop.getEmail(),
                shop.getPhoneNumber()
            ))
            .collect(Collectors.toList());

        User contactUser = tenant.getContactUser();
        return new PendingTenantResponse(
            tenant.getId(),
            tenant.getName(),
            tenant.getDescription(),
            tenant.getContactEmail(),
            contactUser != null ? contactUser.getFullName() : null,
            contactUser != null ? contactUser.getEmail() : null,
            tenant.getContactPhone(),
            tenant.getPrimaryAddress(),
            tenant.getCity(),
            tenant.getState(),
            tenant.getCountry(),
            tenant.getPostalCode(),
            shopInfos,
            tenant.getCreatedDate(),
            tenant.getStatus().toString(),
            tenant.getCompanyRegistration(),
            tenant.getTaxId()
        );
    }

    /**
     * Check if tenant name is available
     */
    public boolean isTenantNameAvailable(String tenantName) {
        return !tenantRepository.existsByNameIgnoreCase(tenantName);
    }

    /**
     * Check if username is available
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsernameIgnoreCase(username) &&
               !keycloakUserService.userExistsByUsername(username);
    }

    /**
     * Check if email is available
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmailIgnoreCase(email) &&
               !keycloakUserService.userExistsByEmail(email);
    }

    /**
     * Publish tenant registration notification event
     */
    private void publishNotificationEvent(Tenant tenant, User contactUser, String clientIp, String userAgent) {
        try {
            TenantRegistrationNotificationEvent notificationEvent = new TenantRegistrationNotificationEvent(
                this,
                tenant.getId(),
                tenant.getName(),
                contactUser.getEmail(),
                contactUser.getFullName(),
                clientIp,
                userAgent
            );

            eventPublisher.publishEvent(notificationEvent);
            log.debug("Published tenant registration notification event for tenant: {}", tenant.getName());

        } catch (Exception e) {
            log.error("Failed to publish notification event for tenant: {}", tenant.getName(), e);
            // Don't throw exception - notification failure shouldn't break registration
        }
    }

    /**
     * Publish tenant activation notification event
     */
    private void publishActivationNotificationEvent(Tenant tenant, boolean approved, String rejectionReason, String adminUserId) {
        try {
            User contactUser = tenant.getContactUser();
            if (contactUser != null) {
                TenantActivationNotificationEvent notificationEvent = new TenantActivationNotificationEvent(
                    this,
                    tenant.getId(),
                    tenant.getName(),
                    contactUser.getEmail(),
                    contactUser.getFullName(),
                    approved,
                    rejectionReason,
                    adminUserId
                );

                eventPublisher.publishEvent(notificationEvent);
                log.debug("Published tenant activation notification event for tenant: {} (approved: {})",
                        tenant.getName(), approved);
            }

        } catch (Exception e) {
            log.error("Failed to publish activation notification event for tenant: {}", tenant.getName(), e);
        }
    }
}