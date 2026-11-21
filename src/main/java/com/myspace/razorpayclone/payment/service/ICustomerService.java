package com.myspace.razorpayclone.payment.service;

import java.util.UUID;

public interface ICustomerService {

    UUID findOrCreate(UUID merchantId, String email, String name, String phone);
}
