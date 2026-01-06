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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom access denied handler for handling authorization failures in the security filter chain.
 * This is invoked when an authenticated user tries to access a resource they don't have permission for.
 *
 * Common scenarios:
 * - User authenticated but lacks required role/permission for the endpoint
 * - OAuth2 resource server access denied (insufficient scope)
 * - Shop-level access control violations at filter level
 *
 * Note: This catches AccessDeniedException from the filter chain (BEFORE DispatcherServlet).
 * For @PreAuthorize exceptions (AFTER DispatcherServlet), GlobalExceptionHandler handles them.
 */
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {

        String errorCode = "FORBIDDEN";
        String errorMessage = accessDeniedException.getMessage() != null && !accessDeniedException.getMessage().isEmpty()
            ? accessDeniedException.getMessage()
            : "You do not have permission to access this resource";

        logger.warn("Access denied for request to {}: {}", request.getRequestURI(), errorMessage);

        // Build JSON error response
        ErrorResponse errorResponse = new ErrorResponse(errorCode, errorMessage);

        // Set response properties
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Write JSON response
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
