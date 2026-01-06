package com.princely.shopmanager.embedded.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for update check response.
 * Contains information about available updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCheckResponse {

    private String currentVersion;
    private String latestVersion;
    private Boolean updateAvailable;
    private LocalDateTime checkedAt;
    private String releaseDate;
    private Map<String, String> downloadUrls;
    private String releaseNotesUrl;
    private String status;
    private String errorMessage;
}
