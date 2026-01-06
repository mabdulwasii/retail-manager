package com.princely.shopmanager.embedded.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for version information from cloud API.
 * Maps to response from /api/registration/latest-version endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionInfo {

    private String version;
    private String releaseDate;
    private Map<String, String> downloadUrls;
    private String releaseNotes;
}
