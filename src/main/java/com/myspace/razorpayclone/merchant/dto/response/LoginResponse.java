package com.myspace.razorpayclone.merchant.dto.response;

public record LoginResponse (
        MerchantResponse merchantResponse,
        Tokens tokens
) {
}
