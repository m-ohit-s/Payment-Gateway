package com.myspace.razorpayclone.merchant.repository;

import com.myspace.razorpayclone.common.enums.MerchantStatus;
import com.myspace.razorpayclone.merchant.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    boolean existsByEmailIgnoreCase(String email);

    List<Merchant> findByMerchantStatus(MerchantStatus merchantStatus);
}
