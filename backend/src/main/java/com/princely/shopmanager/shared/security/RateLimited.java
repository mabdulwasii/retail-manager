package com.princely.shopmanager.shared.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

    /**
     * The maximum number of requests allowed per time period.
     */
    String value() default "100";

    /**
     * The time period for rate limiting (e.g., "1m" for 1 minute, "1s" for 1 second).
     */
    String period() default "1m";

    /**
     * The key to use for rate limiting (e.g., IP address, user ID).
     */
    String key() default "ip";
}