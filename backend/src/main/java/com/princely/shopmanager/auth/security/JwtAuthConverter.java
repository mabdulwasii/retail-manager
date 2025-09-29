package com.princely.shopmanager.auth.security;

import com.princely.shopmanager.shared.domain.JwtPrincipal;
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
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

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

        return new JwtAuthenticationToken(jwt, authorities, getPrincipalClaimName(jwt));
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