package com.tneaguider.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class HalfStepValidator implements ConstraintValidator<HalfStep, BigDecimal> {

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        BigDecimal normalized = value.stripTrailingZeros();
        if (normalized.scale() <= 0) {
            return true;
        }
        BigDecimal doubled = value.multiply(BigDecimal.valueOf(2));
        return doubled.stripTrailingZeros().scale() <= 0;
    }
}
