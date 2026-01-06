package com.princely.shopmanager.embedded.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * mDNS Publisher Service for Embedded Mode.
 * Publishes Shop Manager services on the local network for automatic discovery.
 * Enables professional URLs like http://shopmanager.local instead of localhost:8081.
 *
 * <p>This service is only active when:
 * <ul>
 *   <li>Profile: embedded</li>
 *   <li>Property: application.mdns.enabled = true</li>
 *   <li>Hostname ends with .local (mDNS standard)</li>
 * </ul>
 *
 * <p>Example configuration:
 * <pre>
 * application:
 *   mdns:
 *     enabled: true
 *     hostname: shopmanager.local
 *     service-name: Shop Manager
 * </pre>
 */
@Service
@Profile("embedded")
@ConditionalOnProperty(name = "application.mdns.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class MdnsPublisherService {

    @Value("${application.mdns.hostname:shopmanager.local}")
    private String hostname;

    @Value("${application.mdns.service-name:Shop Manager}")
    private String serviceName;

    @Value("${server.port:8081}")
    private int backendPort;

    @Value("${application.frontend.port:3001}")
    private int frontendPort;

    private JmDNS jmdns;
    private ServiceInfo backendService;
    private ServiceInfo frontendService;

    /**
     * Initialize and publish mDNS services on application startup.
     */
    @PostConstruct
    public void publishServices() {
        if (!hostname.endsWith(".local")) {
            log.warn("mDNS hostname '{}' does not end with '.local' - mDNS may not work correctly", hostname);
            log.info("Recommendation: Use a .local domain for automatic mDNS discovery (e.g., shopmanager.local)");
        }

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            jmdns = JmDNS.create(localHost, hostname);

            publishBackendService();
            publishFrontendService();

            log.info("========================================");
            log.info("mDNS Services Published Successfully");
            log.info("========================================");
            log.info("Hostname: {}", hostname);
            log.info("Backend API: http://{}:{}", hostname, backendPort);
            log.info("Frontend: http://{}:{}", hostname, frontendPort);
            log.info("Services are now discoverable on the local network");
            log.info("========================================");

        } catch (IOException e) {
            log.error("Failed to initialize mDNS publisher", e);
            log.warn("mDNS discovery will not be available. Services can still be accessed via localhost");
        }
    }

    /**
     * Publish backend API service.
     */
    private void publishBackendService() throws IOException {
        Map<String, String> properties = new HashMap<>();
        properties.put("path", "/api");
        properties.put("version", "1.0");
        properties.put("description", serviceName + " API");

        backendService = ServiceInfo.create(
                "_http._tcp.local.",
                serviceName + " API",
                backendPort,
                0, // weight
                0, // priority
                properties
        );

        jmdns.registerService(backendService);
        log.info("Published backend service: {} on port {}", serviceName + " API", backendPort);
    }

    /**
     * Publish frontend web service.
     */
    private void publishFrontendService() throws IOException {
        Map<String, String> properties = new HashMap<>();
        properties.put("path", "/");
        properties.put("description", serviceName + " Web");

        frontendService = ServiceInfo.create(
                "_http._tcp.local.",
                serviceName + " Web",
                frontendPort,
                0, // weight
                0, // priority
                properties
        );

        jmdns.registerService(frontendService);
        log.info("Published frontend service: {} on port {}", serviceName + " Web", frontendPort);
    }

    /**
     * Unregister mDNS services on application shutdown.
     */
    @PreDestroy
    public void unpublishServices() {
        if (jmdns != null) {
            try {
                if (backendService != null) {
                    jmdns.unregisterService(backendService);
                    log.info("Unregistered backend mDNS service");
                }

                if (frontendService != null) {
                    jmdns.unregisterService(frontendService);
                    log.info("Unregistered frontend mDNS service");
                }

                jmdns.close();
                log.info("mDNS publisher closed");

            } catch (Exception e) {
                log.error("Error shutting down mDNS publisher", e);
            }
        }
    }
}
