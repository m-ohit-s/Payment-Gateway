package com.myspace.razorpayclone.merchant.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.NonNull;

public record LoginRequest (
        @NonNull
        @Email
        String email,

        @NotBlank
//        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@$!]){8,}$", message = "password is not in correct format")
        String password
) {
}
