package com.myspace.razorpayclone.payment.api;


import com.myspace.razorpayclone.payment.entity.Payment;

import java.util.List;
import java.util.UUID;

public interface IPaymentLookupService {
    List<Payment> findUnsettledCapturedPayments(UUID merchantId);
}
