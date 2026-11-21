package com.myspace.razorpayclone.common.annotations;

import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface PresentOrFutureYear {
    String message() default "Expiry year should be greater than current year";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
