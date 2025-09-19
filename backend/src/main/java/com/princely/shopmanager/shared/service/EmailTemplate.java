package com.princely.shopmanager.shared.service;

import lombok.Builder;

import java.util.Map;

/**
 * Email template configuration record
 */
@Builder
public record EmailTemplate(
    String to,
    String subject,
    String templateName,
    Map<String, Object> variables
) {
    public EmailTemplate {
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("Email recipient cannot be null or empty");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Email subject cannot be null or empty");
        }
        if (templateName == null || templateName.trim().isEmpty()) {
            throw new IllegalArgumentException("Template name cannot be null or empty");
        }
        if (variables == null) {
            variables = Map.of();
        }
    }
}