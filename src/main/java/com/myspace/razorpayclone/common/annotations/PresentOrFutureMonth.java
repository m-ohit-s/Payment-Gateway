package com.myspace.razorpayclone.common.annotations;

import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface PresentOrFutureMonth {
    String message() default "Expiry month should be greater than current month";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
