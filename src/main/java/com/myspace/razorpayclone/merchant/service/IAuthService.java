package com.myspace.razorpayclone.merchant.service;

import com.myspace.razorpayclone.merchant.dto.request.LoginRequest;
import com.myspace.razorpayclone.merchant.dto.request.MerchantSignupRequest;
import com.myspace.razorpayclone.merchant.dto.response.LoginResponse;
import com.myspace.razorpayclone.merchant.dto.response.MerchantResponse;
import com.myspace.razorpayclone.merchant.dto.response.Tokens;
import jakarta.validation.Valid;

public interface IAuthService {
    MerchantResponse signup(MerchantSignupRequest request);

    LoginResponse login(@Valid LoginRequest loginRequest);

    Tokens refresh(String refreshToken);
}
