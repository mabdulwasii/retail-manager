package com.princely.shopmanager.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.princely.shopmanager.shared.dto.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom authentication entry point for handling authentication failures in the security filter chain.
 * This is invoked when an unauthenticated user tries to access a protected resource.
 *
 * Common scenarios:
 * - Missing JWT token
 * - Expired JWT token
 * - Invalid JWT signature
 * - Malformed JWT token
 *
 * Note: @ControllerAdvice cannot catch these exceptions because they occur in the filter chain
 * before reaching the DispatcherServlet.
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {

        String errorCode = "UNAUTHORIZED";
        String errorMessage = "Authentication failed";

        // Provide more specific error messages based on exception type
        if (authException instanceof InvalidBearerTokenException) {
            InvalidBearerTokenException bearerException = (InvalidBearerTokenException) authException;
            String description = bearerException.getMessage();

            if (description != null) {
                if (description.contains("expired")) {
                    errorCode = "TOKEN_EXPIRED";
                    errorMessage = "JWT token has expired. Please login again.";
                } else if (description.contains("invalid")) {
                    errorCode = "INVALID_TOKEN";
                    errorMessage = "JWT token is invalid.";
                } else if (description.contains("signature")) {
                    errorCode = "INVALID_TOKEN_SIGNATURE";
                    errorMessage = "JWT token signature verification failed.";
                } else {
                    errorMessage = "Invalid bearer token: " + description;
                }
            }

            logger.warn("Bearer token validation failed: {} - {}", errorCode, errorMessage);
        } else {
            // Generic authentication failure
            errorMessage = authException.getMessage() != null && !authException.getMessage().isEmpty()
                ? authException.getMessage()
                : "Authentication is required to access this resource";

            logger.warn("Authentication failed: {}", errorMessage);
        }

        // Build JSON error response
        ErrorResponse errorResponse = new ErrorResponse(errorCode, errorMessage);

        // Set response properties
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Write JSON response
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
