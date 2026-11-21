package com.myspace.razorpayclone.merchant.repository;

import com.myspace.razorpayclone.merchant.entity.AppUser;
import com.myspace.razorpayclone.merchant.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHashAndIsEnabledTrueAndExpiresAtAfter(String tokenHash, LocalDateTime expiresAt);

    List<RefreshToken> findByAppUser(AppUser appUser);
}
