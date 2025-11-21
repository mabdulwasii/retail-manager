package com.princely.shopmanager.shared.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Service for retrieving internationalized messages from properties files
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageSource messageSource;

    /**
     * Get a message by key with no parameters
     *
     * @param key the message key
     * @return the message
     */
    public String getMessage(String key) {
        return getMessage(key, new Object[0]);
    }

    /**
     * Get a message by key with parameters
     *
     * @param key the message key
     * @param params the parameters to substitute into the message
     * @return the message
     */
    public String getMessage(String key, Object... params) {
        return getMessage(key, params, LocaleContextHolder.getLocale());
    }

    /**
     * Get a message by key with parameters and locale
     *
     * @param key the message key
     * @param params the parameters to substitute into the message
     * @param locale the locale
     * @return the message
     */
    public String getMessage(String key, Object[] params, Locale locale) {
        return messageSource.getMessage(key, params, locale);
    }

    /**
     * Get a message by key with default value if not found
     *
     * @param key the message key
     * @param defaultMessage the default message if key not found
     * @return the message or default
     */
    public String getMessageOrDefault(String key, String defaultMessage) {
        return getMessageOrDefault(key, new Object[0], defaultMessage);
    }

    /**
     * Get a message by key with parameters and default value if not found
     *
     * @param key the message key
     * @param params the parameters to substitute into the message
     * @param defaultMessage the default message if key not found
     * @return the message or default
     */
    public String getMessageOrDefault(String key, Object[] params, String defaultMessage) {
        return messageSource.getMessage(key, params, defaultMessage, LocaleContextHolder.getLocale());
    }
}
