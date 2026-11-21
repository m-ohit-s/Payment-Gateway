package com.myspace.razorpayclone.merchant.security;

import com.myspace.razorpayclone.merchant.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final AppUserRepository appUserRepository;

    public boolean validateToken(Long tokenVersion, String email) {
        return appUserRepository.existsByEmailAndTokenVersion(email, tokenVersion);
    }
}
