package com.princely.shopmanager.embedded.security;

import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * JWT token provider for embedded mode.
 * Handles token generation, validation, and parsing.
 */
@Slf4j
@Component
@Profile("embedded")
public class JwtTokenProvider {

    private static final String CLAIM_ROLES = "roles";

    private final SecretKey secretKey;
    private final long jwtExpirationMs;
    private final long refreshExpirationMs;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${application.jwt.secret}") String secret,
            @Value("${application.jwt.expiration-ms}") long jwtExpirationMs,
            @Value("${application.jwt.refresh-expiration-ms}") long refreshExpirationMs,
            @Value("${application.jwt.issuer}") String issuer
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = jwtExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
        this.issuer = issuer;
    }

    /**
     * Generate access token from authentication
     */
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_ROLES, roles)
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Get username from token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Get roles from token
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get(CLAIM_ROLES, List.class);
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generate access token from User entity
     */
    public String generateAccessToken(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        return Jwts.builder()
                .subject(user.getId())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim(CLAIM_ROLES, roles)
                .claim("tenantId", user.getTenantId())
                .claim("shopId", user.getShopId())
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generate refresh token from User entity
     */
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getId())
                .claim("username", user.getUsername())
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Get user ID from token (alias for getUsernameFromToken)
     */
    public String getUserIdFromToken(String token) {
        return getUsernameFromToken(token);
    }

    /**
     * Create JwtPrincipal from token.
     * Extracts all claims and builds a JwtPrincipal object for use with @AuthenticationPrincipal.
     */
    @SuppressWarnings("unchecked")
    public JwtPrincipal getPrincipalFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<String> roles = claims.get(CLAIM_ROLES, List.class);

        return JwtPrincipal.builder()
                .subject(claims.getSubject())
                .userId(claims.getSubject()) // In embedded mode, subject is the user ID
                .preferredUsername(claims.get("username", String.class))
                .email(claims.get("email", String.class))
                .tenantId(claims.get("tenantId", String.class))
                .shopId(claims.get("shopId", String.class))
                .roles(roles != null ? roles : List.of())
                .issuer(claims.getIssuer())
                .issuedAt(claims.getIssuedAt() != null ? claims.getIssuedAt().toInstant() : Instant.now())
                .expiresAt(claims.getExpiration() != null ? claims.getExpiration().toInstant() : null)
                .build();
    }

    /**
     * Get access token validity in milliseconds
     */
    public long getAccessTokenValidity() {
        return jwtExpirationMs;
    }

    /**
     * Get refresh token validity in milliseconds
     */
    public long getRefreshTokenValidity() {
        return refreshExpirationMs;
    }
}
