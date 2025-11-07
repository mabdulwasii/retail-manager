package com.princely.shopmanager.investment.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that an InvestmentCreateRequest has the required fields based on
 * investment type and profit sharing model.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InvestmentCreateRequestValidator.class)
@Documented
public @interface ValidInvestmentCreateRequest {

    String message() default "Invalid investment request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
