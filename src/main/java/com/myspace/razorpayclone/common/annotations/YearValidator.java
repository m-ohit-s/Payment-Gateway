package com.myspace.razorpayclone.common.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

public class YearValidator implements ConstraintValidator<PresentOrFutureYear, Integer> {
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        Integer currentYear = Year.now().getValue();
        return value >= currentYear;
    }
}
