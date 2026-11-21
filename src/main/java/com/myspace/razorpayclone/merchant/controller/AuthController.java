package com.myspace.razorpayclone.merchant.controller;

import com.myspace.razorpayclone.merchant.dto.request.LoginRequest;
import com.myspace.razorpayclone.merchant.dto.request.MerchantSignupRequest;
import com.myspace.razorpayclone.merchant.dto.response.LoginResponse;
import com.myspace.razorpayclone.merchant.dto.response.MerchantResponse;
import com.myspace.razorpayclone.merchant.dto.response.Tokens;
import com.myspace.razorpayclone.merchant.service.IAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<MerchantResponse> signup(@RequestBody @Valid MerchantSignupRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                authService.signup(request)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<MerchantResponse> login(@RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(loginRequest);
        Cookie accessCookie = getSecureCookie("access-token", loginResponse.tokens().accessToken());
        Cookie refreshCookie = getSecureCookie("refresh-token", loginResponse.tokens().refreshToken());
        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
        return ResponseEntity.ok(
                loginResponse.merchantResponse()
        );
    }


    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        Cookie refreshCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals("refresh-token"))
                .findFirst()
                .orElse(null);
        if (refreshCookie == null) {
            throw new RuntimeException("Invalid refresh-token");
        }
        String refreshToken = refreshCookie.getValue();
        Tokens tokens = authService.refresh(refreshToken);
        Cookie accessCookie = getSecureCookie("access-token", tokens.accessToken());
        Cookie newRefreshToken = getSecureCookie("refresh-token", tokens.refreshToken());
        response.addCookie(accessCookie);
        response.addCookie(newRefreshToken);
        return ResponseEntity.ok().build();
    }



    private Cookie getSecureCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
//        cookie.setSecure(true);
        return cookie;
    }
}
