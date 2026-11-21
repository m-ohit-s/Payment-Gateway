package com.myspace.razorpayclone.merchant.repository;

import com.myspace.razorpayclone.merchant.entity.ApiKey;
import com.myspace.razorpayclone.merchant.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    List<ApiKey> findByMerchant_Id(UUID merchantId);

    Optional<ApiKey> findByIdAndMerchant_Id(UUID keyId, UUID merchantId);

    Optional<ApiKey> findByKeyId(String keyId);
}
