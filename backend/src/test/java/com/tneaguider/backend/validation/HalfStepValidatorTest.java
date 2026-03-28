package com.tneaguider.backend.validation;

import com.tneaguider.backend.dto.RecommendationRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HalfStepValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldAllowHalfStepCutoff() {
        RecommendationRequestDto dto = new RecommendationRequestDto(
                new BigDecimal("187.5"), "BC", "CSE", new BigDecimal("100000"), "Chennai"
        );

        Set<ConstraintViolation<RecommendationRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectInvalidDecimalCutoff() {
        RecommendationRequestDto dto = new RecommendationRequestDto(
                new BigDecimal("187.7"), "BC", "CSE", new BigDecimal("100000"), "Chennai"
        );

        Set<ConstraintViolation<RecommendationRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}
