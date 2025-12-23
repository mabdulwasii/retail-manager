package com.princely.shopmanager.auth.security;

import com.princely.shopmanager.shared.domain.JwtPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

import lombok.Getter;

/**
 * Custom JWT Authentication Token that properly handles JwtPrincipal.
 * This extends JwtAuthenticationToken but overrides the principal to be a JwtPrincipal object
 * instead of a String, allowing @AuthenticationPrincipal JwtPrincipal to work correctly.
 */
@Getter
public class CustomJwtAuthenticationToken extends JwtAuthenticationToken {

    private final transient JwtPrincipal jwtPrincipal;

    public CustomJwtAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities, JwtPrincipal principal) {
        super(jwt, authorities, principal.getPreferredUsername());
        this.jwtPrincipal = principal;
    }

    @Override
    public Object getPrincipal() {
        return jwtPrincipal;
    }

}