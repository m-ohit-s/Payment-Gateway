package com.myspace.razorpayclone.merchant.dto.request;

import com.myspace.razorpayclone.common.enums.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MerchantSignupRequest (

        @NotNull(message = "name should be provided")
        @Size(max = 50, message = "name should have at max 50 characters")
        String name,

        @NotNull(message = "email is required")
        @Email(message = "should be a valid email")
        String email,

        @NotNull(message = "password is required")
        @Size(min = 8, message = "password should have at least 8 character")
        String password,

        @Size(max = 50, message = "business name should have at max 50 characters")
        String businessName,

        BusinessType businessType
) {
}
