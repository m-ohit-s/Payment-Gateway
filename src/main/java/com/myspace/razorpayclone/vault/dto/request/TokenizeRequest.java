package com.myspace.razorpayclone.vault.dto.request;

import com.myspace.razorpayclone.common.annotations.PresentOrFutureMonth;
import com.myspace.razorpayclone.common.annotations.PresentOrFutureYear;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.LuhnCheck;

import java.util.UUID;

public record TokenizeRequest (
        @NotBlank(message = "PAN is required")
        @LuhnCheck(message = "Invalid Card Number")
        @Pattern(regexp = "^[0-9]{13,19}$", message = "Invalid PAN length")
        String pan,

        @NotBlank(message = "CVV is required")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "Invalid CVV length")
        String cvv,

        @NotNull(message = "Expiry Month is required")
        @PresentOrFutureMonth
        Integer expiryMonth,

        @NotNull(message = "Expiry Year is required")
        @PresentOrFutureYear
        Integer expiryYear,

        UUID customerId,

        @Size(min = 3, message = "name should have at least 3 characters")
        String cardHolderName
) {
}
