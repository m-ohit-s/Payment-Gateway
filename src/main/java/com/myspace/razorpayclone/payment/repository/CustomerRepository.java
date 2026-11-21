package com.myspace.razorpayclone.payment.repository;

import com.myspace.razorpayclone.merchant.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByMerchant_IdOrEmail(UUID merchantId, String email);
}
