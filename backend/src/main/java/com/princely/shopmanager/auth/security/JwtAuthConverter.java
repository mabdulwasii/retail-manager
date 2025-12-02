package com.princely.shopmanager.auth.security;

import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    private final UserRepository userRepository;

    @Value("${app.keycloak.client-id}")
    private String clientId;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream.concat(
            jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
            extractResourceRoles(jwt).stream()
        ).collect(Collectors.toSet());

        JwtPrincipal principal = JwtPrincipal.fromJwt(jwt);
        List<String> roleNames = extractResourceRoles(jwt).stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
        principal.setRoles(roleNames);

        // Lookup user by Keycloak ID and enrich with database user data
        enrichPrincipalWithUserData(principal);

        return new CustomJwtAuthenticationToken(jwt, authorities, principal);
    }

    /**
     * Enriches the JwtPrincipal with database user data.
     * Looks up the user by Keycloak ID and sets:
     * - Database user ID (always)
     * - Tenant ID (fallback if not in JWT token)
     * - Shop ID (fallback if not in JWT token)
     *
     * This provides a safety net when Keycloak custom attributes aren't configured properly.
     */
    private void enrichPrincipalWithUserData(JwtPrincipal principal) {
        try {
            userRepository.findByKeycloakId(principal.getSubject())
                .ifPresentOrElse(
                    user -> {
                        // Always set database user ID
                        principal.setUserId(user.getId());

                        // Fallback: If Keycloak didn't provide tenantId, get from database
                        if ((principal.getTenantId() == null || principal.getTenantId().trim().isEmpty())
                            && user.getTenant() != null) {
                            principal.setTenantId(user.getTenant().getId());
                            log.debug("Enriched tenantId from database for user: {} (Keycloak didn't provide it)",
                                user.getKeycloakId());
                        }

                        // Fallback: If Keycloak didn't provide shopId, get from database
                        if ((principal.getShopId() == null || principal.getShopId().trim().isEmpty())
                            && user.getShop() != null) {
                            principal.setShopId(user.getShop().getId());
                            log.debug("Enriched shopId from database for user: {} (Keycloak didn't provide it)",
                                user.getKeycloakId());
                        }
                    },
                    () -> log.warn("User not found in database for Keycloak ID: {}. User data will not be enriched.",
                        principal.getSubject())
                );
        } catch (Exception e) {
            log.error("Failed to enrich principal with user data for Keycloak ID: {}. " +
                "User ID, tenant ID, and shop ID may not be set correctly.",
                principal.getSubject(), e);
        }
    }

    private String getPrincipalClaimName(Jwt jwt) {
        String claimName = "preferred_username";
        return jwt.getClaim(claimName);
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Extract realm roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            Collection<String> realmRoles = (Collection<String>) realmAccess.get("roles");
            if (realmRoles != null) {
                authorities.addAll(realmRoles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toSet()));
            }
        }

        // Extract resource/client roles
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            Map<String, Object> clientResource = (Map<String, Object>) resourceAccess.get(clientId);
            if (clientResource != null) {
                Collection<String> clientRoles = (Collection<String>) clientResource.get("roles");
                if (clientRoles != null) {
                    authorities.addAll(clientRoles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toSet()));
                }
            }
        }

        return authorities;
    }
}