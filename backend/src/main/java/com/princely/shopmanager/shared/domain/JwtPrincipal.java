package com.princely.shopmanager.shared.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtPrincipal {

    private String subject; // Keycloak user ID from JWT 'sub' claim
    private String userId; // Database User ID (set after user lookup/sync)
    private String preferredUsername;
    private String tenantId;

    /**
     * Shop ID assigned to the user.
     *
     * CURRENT LIMITATION: Single shop per user
     * - Users can currently be assigned to only ONE shop (User.shop is ManyToOne)
     * - This field stores a single shop ID from the JWT token
     * - TENANT_ADMIN and SYSTEM_ADMIN roles have access to ALL shops in their tenant
     *
     * FUTURE ENHANCEMENT: Multi-shop support
     * - Change User.shop to ManyToMany relationship
     * - Change this field to List<String> shopIds
     * - Update Keycloak mapper to return shop_ids as array
     * - Update all shop access validation logic
     */
    private String shopId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private List<String> roles;
    private Map<String, Object> claims;
    private Instant issuedAt;
    private Instant expiresAt;
    private String issuer;
    private String sessionState;
    private String scope;

    @SuppressWarnings("unchecked")
    public static JwtPrincipal fromJwt(Jwt jwt) {
        List<String> roles = new ArrayList<>();

        // Try to extract roles from realm_access
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.get("roles") != null) {
            Object rolesObj = realmAccess.get("roles");
            if (rolesObj instanceof List) {
                roles.addAll((List<String>) rolesObj);
            }
        }

        // If no realm roles found, try direct roles claim
        if (roles.isEmpty()) {
            List<String> directRoles = jwt.getClaimAsStringList("roles");
            if (directRoles != null) {
                roles.addAll(directRoles);
            }
        }

        return JwtPrincipal.builder()
            .subject(jwt.getClaimAsString("sub"))
            .preferredUsername(jwt.getClaimAsString("preferred_username"))
            .tenantId(jwt.getClaimAsString("tenant_id"))
            .shopId(jwt.getClaimAsString("shop_id"))
            .email(jwt.getClaimAsString("email"))
            .firstName(jwt.getClaimAsString("given_name"))
            .lastName(jwt.getClaimAsString("family_name"))
            .phoneNumber(jwt.getClaimAsString("phone_number"))
            .roles(roles)
            .claims(jwt.getClaims())
            .issuedAt(jwt.getIssuedAt())
            .expiresAt(jwt.getExpiresAt())
            .issuer(jwt.getClaimAsString("iss"))
            .sessionState(jwt.getClaimAsString("session_state"))
            .scope(jwt.getClaimAsString("scope"))
            .build();
    }

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        if (firstName != null) {
            return firstName;
        }
        if (lastName != null) {
            return lastName;
        }
        return preferredUsername != null ? preferredUsername : email;
    }

    public boolean isValid() {
        return subject != null &&
               expiresAt != null &&
               expiresAt.isAfter(Instant.now());
    }

    public boolean hasTenant() {
        return tenantId != null && !tenantId.trim().isEmpty();
    }

    @SuppressWarnings("unchecked")
    public <T> T getClaim(String name, Class<T> type) {
        if (claims == null) {
            return null;
        }
        Object value = claims.get(name);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }

    public String getClaimAsString(String name) {
        return getClaim(name, String.class);
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public String getUsername() {
        return preferredUsername != null ? preferredUsername : email;
    }

    /**
     * Get the database User ID.
     * This is set after user lookup/sync and represents the internal database user ID.
     * For Keycloak ID, use getSubject().
     *
     * @return Database user ID, or null if not yet set
     */
    public String getUserId() {
        return userId;
    }
}