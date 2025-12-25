package com.princely.shopmanager.embedded.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Configuration for Single Page Application (SPA) routing in embedded mode.
 *
 * <p>This controller handles 404 errors by forwarding to index.html,
 * allowing React Router to handle client-side routing properly.
 *
 * <p>Only active in embedded profile where frontend is bundled with backend.
 */
@Slf4j
@Controller
@Profile("embedded")
public class SpaRoutingConfig implements ErrorController {

    /**
     * Handle errors, particularly 404s, by forwarding to index.html for React Router.
     *
     * <p>This ensures that page refreshes on client-side routes work correctly.
     * API requests and static resources are not affected.
     *
     * @param request the HTTP request
     * @return forward to index.html for 404 errors, error view otherwise
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            // Forward 404 errors to index.html for SPA routing
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

                // Don't forward API requests or actual static resource requests
                if (path != null && !path.startsWith("/api") && !path.startsWith("/actuator")) {
                    log.debug("Forwarding 404 request {} to index.html for SPA routing", path);
                    return "forward:/index.html";
                }
            }
        }

        // For other errors, return error view
        return "error";
    }
}
