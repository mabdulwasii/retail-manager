package com.princely.shopmanager.embedded.service;

import com.princely.shopmanager.embedded.dto.UpdateCheckResponse;
import com.princely.shopmanager.embedded.dto.VersionInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Update Check Service for Embedded Mode.
 * Periodically checks for new Shop Manager versions from the cloud API.
 *
 * <p>This service is only active when:
 * <ul>
 *   <li>Profile: embedded</li>
 *   <li>Property: application.update-check.enabled = true</li>
 * </ul>
 *
 * <p>Features:
 * <ul>
 *   <li>Scheduled version check (configurable via cron)</li>
 *   <li>Manual check via {@link #checkForUpdates()}</li>
 *   <li>Cached result to avoid excessive API calls</li>
 *   <li>Version comparison using semantic versioning</li>
 * </ul>
 */
@Service
@Profile("embedded")
@ConditionalOnProperty(name = "application.update-check.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
public class UpdateCheckService {

    private static final String LOG_SEPARATOR = "========================================";

    @Value("${application.version:1.0.0}")
    private String currentVersion;

    @Value("${application.cloud-api-url:https://api.retailhq.app}")
    private String cloudApiUrl;

    private final RestClient.Builder restClientBuilder;

    private final AtomicReference<UpdateCheckResponse> cachedResponse = new AtomicReference<>();

    /**
     * Scheduled update check (runs every 24 hours by default).
     * Cron expression is configurable via application.update-check.cron property.
     */
    @Scheduled(cron = "${application.update-check.cron:0 0 */24 * * ?}")
    public void scheduledUpdateCheck() {
        log.info("Running scheduled update check...");
        checkForUpdates();
    }

    /**
     * Manually trigger an update check.
     * Can be called from the controller for user-initiated checks.
     *
     * @return UpdateCheckResponse with version information
     */
    public UpdateCheckResponse checkForUpdates() {
        log.info("Checking for updates from cloud API: {}", cloudApiUrl);

        try {
            RestClient restClient = restClientBuilder.build();
            VersionInfo versionInfo = restClient.get()
                    .uri(cloudApiUrl + "/api/registration/latest-version")
                    .retrieve()
                    .body(VersionInfo.class);

            if (versionInfo == null) {
                log.error("Received null response from cloud API");
                return buildErrorResponse("Failed to retrieve version information");
            }

            boolean updateAvailable = isUpdateAvailable(currentVersion, versionInfo.getVersion());

            UpdateCheckResponse response = UpdateCheckResponse.builder()
                    .currentVersion(currentVersion)
                    .latestVersion(versionInfo.getVersion())
                    .updateAvailable(updateAvailable)
                    .checkedAt(LocalDateTime.now())
                    .releaseDate(versionInfo.getReleaseDate())
                    .downloadUrls(versionInfo.getDownloadUrls())
                    .releaseNotesUrl(versionInfo.getReleaseNotes())
                    .status("SUCCESS")
                    .build();

            cachedResponse.set(response);

            if (updateAvailable) {
                log.info(LOG_SEPARATOR);
                log.info("UPDATE AVAILABLE");
                log.info(LOG_SEPARATOR);
                log.info("Current Version: {}", currentVersion);
                log.info("Latest Version: {}", versionInfo.getVersion());
                log.info("Release Date: {}", versionInfo.getReleaseDate());
                log.info("Release Notes: {}", versionInfo.getReleaseNotes());
                log.info(LOG_SEPARATOR);
            } else {
                log.info("No update available. Current version {} is up to date.", currentVersion);
            }

            return response;

        } catch (Exception e) {
            log.error("Error checking for updates", e);
            return buildErrorResponse("Error checking for updates: " + e.getMessage());
        }
    }

    /**
     * Get the cached update check result.
     * Returns the last check result without making a new API call.
     *
     * @return Cached UpdateCheckResponse or null if no check has been performed
     */
    public UpdateCheckResponse getCachedStatus() {
        return cachedResponse.get();
    }

    /**
     * Compare two semantic versions to determine if an update is available.
     * Supports format: MAJOR.MINOR.PATCH (e.g., 0.1.28, 1.2.3)
     *
     * @param current Current version string
     * @param latest Latest version string
     * @return true if latest is newer than current
     */
    private boolean isUpdateAvailable(String current, String latest) {
        try {
            String[] currentParts = current.split("\\.");
            String[] latestParts = latest.split("\\.");

            for (int i = 0; i < Math.min(currentParts.length, latestParts.length); i++) {
                int currentPart = Integer.parseInt(currentParts[i]);
                int latestPart = Integer.parseInt(latestParts[i]);

                if (latestPart > currentPart) {
                    return true;
                } else if (latestPart < currentPart) {
                    return false;
                }
            }

            // If all parts are equal, check if latest has more parts
            return latestParts.length > currentParts.length;

        } catch (Exception e) {
            log.error("Error comparing versions: current={}, latest={}", current, latest, e);
            return false;
        }
    }

    /**
     * Build an error response when update check fails.
     */
    private UpdateCheckResponse buildErrorResponse(String errorMessage) {
        UpdateCheckResponse errorResponse = UpdateCheckResponse.builder()
                .currentVersion(currentVersion)
                .updateAvailable(false)
                .checkedAt(LocalDateTime.now())
                .status("ERROR")
                .errorMessage(errorMessage)
                .build();

        cachedResponse.set(errorResponse);
        return errorResponse;
    }
}
