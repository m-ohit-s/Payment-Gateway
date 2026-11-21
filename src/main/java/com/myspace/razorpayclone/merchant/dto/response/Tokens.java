package com.myspace.razorpayclone.merchant.dto.response;

public record Tokens (
        String accessToken,
        String refreshToken
){
}
