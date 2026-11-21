package com.myspace.razorpayclone.payment.service.impl;

import com.myspace.razorpayclone.common.exceptions.ResourceNotFoundException;
import com.myspace.razorpayclone.merchant.entity.Customer;
import com.myspace.razorpayclone.merchant.entity.Merchant;
import com.myspace.razorpayclone.merchant.repository.MerchantRepository;
import com.myspace.razorpayclone.payment.repository.CustomerRepository;
import com.myspace.razorpayclone.payment.service.ICustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService implements ICustomerService {

    private final CustomerRepository customerRepository;
    private final MerchantRepository merchantRepository;

    @Override
    @Transactional
    public UUID findOrCreate(UUID merchantId, String email, String name, String phone) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return customerRepository.findByMerchant_IdOrEmail(merchantId, email).map(
                Customer::getId
        ).orElseGet(() -> createNew(merchantId, email, name, phone));
    }

    private UUID createNew(UUID merchantId, String email, String name, String phone) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(
                () -> new ResourceNotFoundException("MERCHANT", merchantId)
        );

        Customer customer = Customer.builder()
                .merchant(merchant)
                .name(name)
                .email(email)
                .contactNumber(phone)
                .build();
        customer = customerRepository.save(customer);
        log.info("Customer Created with email : {} and merchant : {}", email, merchant.getId().toString());
        return customer.getId();
    }
}
