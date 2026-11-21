package com.myspace.razorpayclone.common.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

public class MonthValidator implements ConstraintValidator<PresentOrFutureMonth, Integer> {
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        Integer currentMonth = Year.now().getValue();
        return value >= currentMonth && value <= 12;
    }
}
