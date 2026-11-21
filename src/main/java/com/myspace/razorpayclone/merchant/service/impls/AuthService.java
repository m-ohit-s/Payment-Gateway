package com.myspace.razorpayclone.merchant.service.impls;

import com.myspace.razorpayclone.common.enums.MerchantStatus;
import com.myspace.razorpayclone.common.enums.UserRole;
import com.myspace.razorpayclone.common.exceptions.DuplicateResourceException;
import com.myspace.razorpayclone.common.exceptions.ResourceNotFoundException;
import com.myspace.razorpayclone.common.util.RandomizerUtil;
import com.myspace.razorpayclone.merchant.dto.request.LoginRequest;
import com.myspace.razorpayclone.merchant.dto.request.MerchantSignupRequest;
import com.myspace.razorpayclone.merchant.dto.response.LoginResponse;
import com.myspace.razorpayclone.merchant.dto.response.MerchantResponse;
import com.myspace.razorpayclone.merchant.dto.response.Tokens;
import com.myspace.razorpayclone.merchant.entity.AppUser;
import com.myspace.razorpayclone.merchant.entity.Merchant;
import com.myspace.razorpayclone.merchant.entity.RefreshToken;
import com.myspace.razorpayclone.merchant.mapper.MerchantMapper;
import com.myspace.razorpayclone.merchant.repository.AppUserRepository;
import com.myspace.razorpayclone.merchant.repository.MerchantRepository;
import com.myspace.razorpayclone.merchant.repository.RefreshTokenRepository;
import com.myspace.razorpayclone.merchant.security.JwtUtil;
import com.myspace.razorpayclone.merchant.security.TokenHashUtil;
import com.myspace.razorpayclone.merchant.service.IAuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {

    private final MerchantRepository merchantRepository;
    private final AppUserRepository appUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MerchantMapper merchantMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public MerchantResponse signup(MerchantSignupRequest request) {

        if (merchantRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("DUPLICATE_MERCHANT", "Merchant with email already exists");
        }

        Merchant merchant = Merchant.builder()
                .name(request.name())
                .email(request.email())
                .businessType(request.businessType())
                .businessName(request.businessName())
                .merchantStatus(MerchantStatus.PENDING_KYC)
                .build();

        merchant = merchantRepository.save(merchant);

        AppUser appUser = AppUser.builder()
                .merchant(merchant)
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.OWNER)
                .build();

        appUserRepository.save(appUser);

        return merchantMapper.toMerchantResponse(merchant);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.email(), loginRequest.password()
        ));

        if (!authentication.isAuthenticated()) {
            throw new BadCredentialsException("Bad credentials");
        }

        AppUser appUser = appUserRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new ResourceNotFoundException("USER", loginRequest.email()));
        Merchant merchant = appUser.getMerchant();
        String accessToken = jwtUtil.generateAccessToken(
                appUser.getEmail(),
                merchant.getId(),
                appUser.getRole().name(),
                appUser.getTokenVersion()
        );
        String refreshToken = RandomizerUtil.randomBase64(32);

        RefreshToken token = RefreshToken.builder()
                .tokenHash(TokenHashUtil.encryptToken(refreshToken))
                .appUser(appUser)
                .build();
        refreshTokenRepository.save(token);

        return new LoginResponse(
                merchantMapper.toMerchantResponse(merchant),
                new Tokens(accessToken, refreshToken)
        );
    }

    @Override
    @Transactional
    public Tokens refresh(String refreshToken) {
        String tokenHash = TokenHashUtil.encryptToken(refreshToken);
        RefreshToken token = refreshTokenRepository.findByTokenHashAndIsEnabledTrueAndExpiresAtAfter(tokenHash, LocalDateTime.now())
                .orElseThrow(() -> new ResourceNotFoundException("REFRESH_TOKEN", tokenHash));
        token.setEnabled(false);
        AppUser appUser = token.getAppUser();
        String accessToken = jwtUtil.generateAccessToken(
                appUser.getEmail(),
                appUser.getMerchant().getId(),
                appUser.getRole().name(),
                appUser.getTokenVersion()
        );
        String newRefreshToken = RandomizerUtil.randomBase64(32);
        RefreshToken newToken = RefreshToken.builder()
                .tokenHash(TokenHashUtil.encryptToken(newRefreshToken))
                .appUser(appUser)
                .build();
        refreshTokenRepository.save(newToken);
        return new Tokens(accessToken, newRefreshToken);
    }
}
